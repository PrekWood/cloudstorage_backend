package unipi.cloudstorage.countryCodes;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/api/country-codes")
public class CountryCodeController {

    private final CountryCodeService countryCodeService;

    @GetMapping
    public List<CountryCode> getAllCountryCodes(){
        return countryCodeService.getAllCountryCodes();
    }
}
