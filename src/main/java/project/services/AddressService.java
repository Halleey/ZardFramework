package project.services;

import configurations.instancias.Service;
import project.dtos.AddresRequestDto;
import project.entities.Address;
import project.repositories.AddressRepository;

@Service
public class AddressService {
    private  final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public void saveAddress(AddresRequestDto requestDto) {
        Address address = new Address();
        address.setStreet(requestDto.getStreet());
        address.setCity(requestDto.getCity());
        addressRepository.save(address);
    }


}


