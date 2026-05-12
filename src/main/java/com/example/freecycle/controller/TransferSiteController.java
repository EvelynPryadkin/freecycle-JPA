package com.example.freecycle.controller;

import com.example.freecycle.dto.CreateTimeSlotRequest;
import com.example.freecycle.entity.TimeSlot;
import com.example.freecycle.entity.TransferSite;
import com.example.freecycle.exception.NotFoundException;
import com.example.freecycle.repository.TimeSlotRepository;
import com.example.freecycle.repository.TransferSiteRepository;
import com.example.freecycle.service.FreecycleService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TransferSiteController {

    private final TransferSiteRepository transferSiteRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final FreecycleService service;

    public TransferSiteController(
            TransferSiteRepository transferSiteRepo,
            TimeSlotRepository timeSlotRepo,
            FreecycleService service
    ) {
        this.transferSiteRepo = transferSiteRepo;
        this.timeSlotRepo = timeSlotRepo;
        this.service = service;
    }

    @GetMapping("/transfer-sites")
    public List<TransferSite> findAllSites() {
        return transferSiteRepo.findAll();
    }

    @GetMapping("/transfer-sites/{siteId}")
    public TransferSite findSite(@PathVariable Long siteId) {
        return transferSiteRepo.findById(siteId)
                .orElseThrow(() -> new NotFoundException("Transfer site not found."));
    }

    @PostMapping("/transfer-sites")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferSite createSite(@RequestBody TransferSite transferSite) {
        transferSite.setId(null);
        return transferSiteRepo.save(transferSite);
    }

    @GetMapping("/transfer-sites/{siteId}/time-slots")
    public List<TimeSlot> findTimeSlotsForSite(@PathVariable Long siteId) {
        return timeSlotRepo.findByTransferSiteId(siteId);
    }

    @GetMapping("/time-slots")
    public List<TimeSlot> findAllTimeSlots() {
        return timeSlotRepo.findAll();
    }

    @PostMapping("/time-slots")
    @ResponseStatus(HttpStatus.CREATED)
    public TimeSlot createTimeSlot(@RequestBody CreateTimeSlotRequest request) {
        return service.createTimeSlot(request);
    }
}
