package unipi.cloudstorage.countryCodes;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import unipi.cloudstorage.countryCodes.exceptions.CountryCodeNotFoundException;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CountryCodeService {

    private final CountryCodeRepository countryCodeRepository;

    public List<CountryCode> getAllCountryCodes(){
        return countryCodeRepository.findAll();
    }

    public CountryCode findById(Long id) throws CountryCodeNotFoundException {
        Optional<CountryCode> result = countryCodeRepository.findById(id);
        if(result.isPresent()){
            return result.get();
        }else{
            throw new CountryCodeNotFoundException("not found");
        }
    }
}
