package com.example.freecycle.service;

import com.example.freecycle.dto.CreateInterestRequest;
import com.example.freecycle.dto.CreateItemRequest;
import com.example.freecycle.dto.CreateMessageRequest;
import com.example.freecycle.dto.CreateTimeSlotRequest;
import com.example.freecycle.dto.CreateUserRequest;
import com.example.freecycle.dto.ScheduleRequest;
import com.example.freecycle.entity.Appointment;
import com.example.freecycle.entity.AppointmentStatus;
import com.example.freecycle.entity.InterestStatus;
import com.example.freecycle.entity.Item;
import com.example.freecycle.entity.ItemInterest;
import com.example.freecycle.entity.ItemState;
import com.example.freecycle.entity.Message;
import com.example.freecycle.entity.TimeSlot;
import com.example.freecycle.entity.TransferSite;
import com.example.freecycle.entity.User;
import com.example.freecycle.exception.BadRequestException;
import com.example.freecycle.exception.NotFoundException;
import com.example.freecycle.repository.AppointmentRepository;
import com.example.freecycle.repository.ItemInterestRepository;
import com.example.freecycle.repository.ItemRepository;
import com.example.freecycle.repository.MessageRepository;
import com.example.freecycle.repository.TimeSlotRepository;
import com.example.freecycle.repository.TransferSiteRepository;
import com.example.freecycle.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FreecycleService {

    private final UserRepository userRepo;
    private final TransferSiteRepository transferSiteRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final ItemRepository itemRepo;
    private final ItemInterestRepository interestRepo;
    private final AppointmentRepository appointmentRepo;
    private final MessageRepository messageRepo;

    public FreecycleService(
            UserRepository userRepo,
            TransferSiteRepository transferSiteRepo,
            TimeSlotRepository timeSlotRepo,
            ItemRepository itemRepo,
            ItemInterestRepository interestRepo,
            AppointmentRepository appointmentRepo,
            MessageRepository messageRepo
    ) {
        this.userRepo = userRepo;
        this.transferSiteRepo = transferSiteRepo;
        this.timeSlotRepo = timeSlotRepo;
        this.itemRepo = itemRepo;
        this.interestRepo = interestRepo;
        this.appointmentRepo = appointmentRepo;
        this.messageRepo = messageRepo;
    }

    public User createUser(CreateUserRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(request.password());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        user.setAddress(request.address());
        return userRepo.save(user);
    }

    public Item createItem(CreateItemRequest request) {
        User donor = getUser(request.donorId());
        Item item = new Item();
        item.setDonor(donor);
        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setCategory(request.category());
        item.setCondition(request.condition());
        item.setSize(request.size());
        item.setQuantity(request.quantity() == null ? 1 : request.quantity());
        item.setState(ItemState.POSTED);
        return itemRepo.save(item);
    }

    public ItemInterest createInterest(Long itemId, CreateInterestRequest request) {
        Item item = getItem(itemId);
        if (item.getState() == ItemState.DONE) {
            throw new BadRequestException("Cannot express interest in an item that is done.");
        }
        User user = getUser(request.userId());
        if (item.getDonor().getId().equals(user.getId())) {
            throw new BadRequestException("Donor cannot express interest in their own item.");
        }
        if (interestRepo.findByItemIdAndUserId(itemId, user.getId()).isPresent()) {
            throw new BadRequestException("This user already expressed interest in this item.");
        }

        ItemInterest interest = new ItemInterest();
        interest.setItem(item);
        interest.setUser(user);
        interest.setMessage(request.message());
        interest.setStatus(InterestStatus.PENDING);
        ItemInterest saved = interestRepo.save(interest);

        createMessage(
                user.getId(),
                item.getDonor().getId(),
                item,
                "New interest in your item",
                user.getFirstName() + " expressed interest in " + item.getTitle() + "."
        );

        return saved;
    }

    public TimeSlot createTimeSlot(CreateTimeSlotRequest request) {
        TransferSite site = transferSiteRepo.findById(request.transferSiteId())
                .orElseThrow(() -> new NotFoundException("Transfer site not found."));
        if (request.endTime() == null || request.startTime() == null || !request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time.");
        }

        TimeSlot slot = new TimeSlot();
        slot.setTransferSite(site);
        slot.setStartTime(request.startTime());
        slot.setEndTime(request.endTime());
        slot.setMaxCapacity(request.maxCapacity() == null ? 10 : request.maxCapacity());
        return timeSlotRepo.save(slot);
    }

    public Message createMessage(CreateMessageRequest request) {
        Item item = request.itemId() == null ? null : getItem(request.itemId());
        getUser(request.senderId());
        getUser(request.recipientId());
        return createMessage(request.senderId(), request.recipientId(), item, request.subject(), request.content());
    }

    @Transactional
    public Item selectRecipient(Long itemId, Long interestId) {
        Item item = getItem(itemId);
        ItemInterest selectedInterest = getInterest(interestId);
        if (!selectedInterest.getItem().getId().equals(itemId)) {
            throw new BadRequestException("Interest does not belong to this item.");
        }

        List<ItemInterest> interests = interestRepo.findByItemId(itemId);
        for (ItemInterest interest : interests) {
            interest.setStatus(interest.getId().equals(interestId) ? InterestStatus.SELECTED : InterestStatus.PENDING);
        }

        appointmentRepo.deleteByItemId(itemId);
        item.setState(ItemState.PENDING);

        interestRepo.saveAll(interests);
        Item saved = itemRepo.save(item);

        createMessage(
                item.getDonor().getId(),
                selectedInterest.getUser().getId(),
                item,
                "You were selected",
                "The donor selected you for " + item.getTitle() + "."
        );

        return saved;
    }

    @Transactional
    public Item deselectRecipient(Long itemId) {
        Item item = getItem(itemId);
        ItemInterest selected = getSelectedInterestOrNull(itemId);

        appointmentRepo.deleteByItemId(itemId);

        List<ItemInterest> interests = interestRepo.findByItemId(itemId);
        for (ItemInterest interest : interests) {
            interest.setStatus(InterestStatus.PENDING);
        }

        item.setState(ItemState.POSTED);
        interestRepo.saveAll(interests);
        Item saved = itemRepo.save(item);

        if (selected != null) {
            createMessage(
                    item.getDonor().getId(),
                    selected.getUser().getId(),
                    item,
                    "Recipient selection removed",
                    "The donor removed the recipient selection for " + item.getTitle() + "."
            );
        }

        return saved;
    }

    @Transactional
    public Appointment schedule(Long itemId, ScheduleRequest request) {
        Item item = getItem(itemId);
        ItemInterest selected = getSelectedInterest(itemId);
        TimeSlot timeSlot = timeSlotRepo.findById(request.timeSlotId())
                .orElseThrow(() -> new NotFoundException("Time slot not found."));

        appointmentRepo.deleteByItemId(itemId);

        Appointment appointment = new Appointment();
        appointment.setItem(item);
        appointment.setDonor(item.getDonor());
        appointment.setRecipient(selected.getUser());
        appointment.setTimeSlot(timeSlot);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes(request.notes());

        item.setState(ItemState.SCHEDULED);
        itemRepo.save(item);
        Appointment saved = appointmentRepo.save(appointment);

        createMessage(
                item.getDonor().getId(),
                selected.getUser().getId(),
                item,
                "Transfer scheduled",
                "A transfer meeting was scheduled for " + item.getTitle() + "."
        );

        return saved;
    }

    @Transactional
    public Item complete(Long itemId) {
        Item item = getItem(itemId);
        ItemInterest selected = getSelectedInterestOrNull(itemId);

        appointmentRepo.deleteByItemId(itemId);
        interestRepo.deleteByItemId(itemId);

        item.setState(ItemState.DONE);
        Item saved = itemRepo.save(item);

        if (selected != null) {
            createMessage(
                    item.getDonor().getId(),
                    selected.getUser().getId(),
                    item,
                    "Transfer completed",
                    "The transfer for " + item.getTitle() + " was marked complete."
            );
        }

        return saved;
    }

    @Transactional
    public Item deschedule(Long itemId) {
        Item item = getItem(itemId);
        ItemInterest selected = getSelectedInterest(itemId);

        appointmentRepo.deleteByItemId(itemId);
        item.setState(ItemState.PENDING);
        Item saved = itemRepo.save(item);

        createMessage(
                item.getDonor().getId(),
                selected.getUser().getId(),
                item,
                "Transfer meeting cancelled",
                "The scheduled meeting for " + item.getTitle() + " was cancelled. You are still selected."
        );

        return saved;
    }

    @Transactional
    public void cancelOffer(Long itemId) {
        Item item = getItem(itemId);
        List<ItemInterest> interests = interestRepo.findByItemId(itemId);
        for (ItemInterest interest : interests) {
            createMessage(
                    item.getDonor().getId(),
                    interest.getUser().getId(),
                    item,
                    "Offer cancelled",
                    "The donor cancelled the offer for " + item.getTitle() + "."
            );
        }
        appointmentRepo.deleteByItemId(itemId);
        interestRepo.deleteByItemId(itemId);
        itemRepo.deleteById(itemId);
    }

    @Transactional
    public void withdrawInterest(Long itemId, Long interestId) {
        Item item = getItem(itemId);
        ItemInterest interest = getInterest(interestId);
        if (!interest.getItem().getId().equals(itemId)) {
            throw new BadRequestException("Interest does not belong to this item.");
        }

        boolean selected = interest.getStatus() == InterestStatus.SELECTED;
        if (selected) {
            appointmentRepo.deleteByItemId(itemId);
            item.setState(ItemState.POSTED);
            itemRepo.save(item);
        }

        interestRepo.delete(interest);

        createMessage(
                interest.getUser().getId(),
                item.getDonor().getId(),
                item,
                "Interest withdrawn",
                interest.getUser().getFirstName() + " withdrew interest in " + item.getTitle() + "."
        );
    }

    public Message markMessageRead(Long messageId) {
        Message message = messageRepo.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found."));
        message.setRead(true);
        message.setReadAt(LocalDateTime.now());
        return messageRepo.save(message);
    }

    private User getUser(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new NotFoundException("User not found."));
    }

    private Item getItem(Long id) {
        return itemRepo.findById(id).orElseThrow(() -> new NotFoundException("Item not found."));
    }

    private ItemInterest getInterest(Long id) {
        return interestRepo.findById(id).orElseThrow(() -> new NotFoundException("Interest not found."));
    }

    private ItemInterest getSelectedInterest(Long itemId) {
        return interestRepo.findByItemIdAndStatus(itemId, InterestStatus.SELECTED)
                .orElseThrow(() -> new BadRequestException("Select a recipient before scheduling."));
    }

    private ItemInterest getSelectedInterestOrNull(Long itemId) {
        return interestRepo.findByItemIdAndStatus(itemId, InterestStatus.SELECTED).orElse(null);
    }

    private Message createMessage(Long senderId, Long recipientId, Item item, String subject, String content) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setRecipientId(recipientId);
        message.setItem(item);
        message.setSubject(subject);
        message.setContent(content);
        return messageRepo.save(message);
    }
}
