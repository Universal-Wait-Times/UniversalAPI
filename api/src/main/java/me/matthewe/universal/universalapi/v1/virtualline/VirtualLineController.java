package me.matthewe.universal.universalapi.v1.virtualline;

import me.matthewe.universal.commons.ResortRegion;
import me.matthewe.universal.commons.virtualline.VirtualLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/api/v1/virtual_line")
@RestController()
public class VirtualLineController {
    private VirtualLineService virtualLineService;

    @Autowired
    public VirtualLineController(VirtualLineService virtualLineService) {
        this.virtualLineService = virtualLineService;
    }

    @GetMapping("/{resort}/active")
    public List<VirtualLine> getLines(@PathVariable  String resort) {
        ResortRegion resortRegion = ResortRegion.getByName(resort);
        List<VirtualLine> virtualLines = new ArrayList<>();

        if  (resortRegion==null)return virtualLines;

        if (resortRegion==ResortRegion.USJ) return virtualLines;


        if( resortRegion==ResortRegion.USH) {
            return virtualLineService.getCachedData("Hollywood").stream().filter(VirtualLine::isEnabled).toList();
        }
        return virtualLineService.getCachedData("Orlando").stream().filter(VirtualLine::isEnabled).toList();

    }
}

