package services;

import dtos.UserRequestDto;

import entities.Address;
import entities.Users;

import repositories.AddressRepository;
import repositories.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository repository;
    private final AddressRepository addressRepository;
    public UserService(UserRepository repository, AddressRepository addressRepository) {
        this.repository = repository;

        this.addressRepository = addressRepository;
    }

    public void createUser(UserRequestDto  requestDto) {
        Users user = new Users();
        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());
        user.setCpf(requestDto.getCpf());
        Address address = addressRepository.findById(requestDto.getAddress_id()).orElseThrow(() -> new RuntimeException("não tem esse id"));
        user.setAddress(address);
        System.out.println("Address recuperado: " + address);
        repository.save(user);
    }

    public List<Users> getAll() {
      return repository.findAll();

    }

    public boolean deleteUser(Long id) {
        Optional<Users> user = repository.findById(id);
        if (user.isPresent()) {
            repository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
