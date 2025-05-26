package project.services;

import configurations.cripted.HashPassword;
import configurations.instancias.Service;
import project.dtos.CheckPasswordDto;
import project.dtos.UserRequestDto;

import entities.Address;
import entities.Users;

import project.dtos.UserResponseDTO;
import project.repositories.AddressRepository;
import project.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
@Service
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
        String password = HashPassword.gerarHash(requestDto.getPassword());
        user.setPassword(password);
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

    //fix controller for support with get params
    public Optional<Users> getUser(Long id){
        return repository.findById(id);
    }

    public List<Users> getUserById(Long address_id){
        return repository.findUsersByAddressId(address_id);
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
    public List<UserResponseDTO> getUsersByName(String name) {
        return repository.findUsersByName(name);
    }

    @Override
    public String toString() {
        return "UserService [repository=" + repository + ", addressRepository=" + addressRepository + "]";
    }

    public boolean CheckPassword(CheckPasswordDto passwordDto){
       Users users = repository.findByName(passwordDto.getName());
        return HashPassword.verificarSenha(passwordDto.getPassword(), users.getPassword());
    }


}
