package services;

import dtos.AddresRequestDto;
import entities.Address;
import repositories.AddressRepository;

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


