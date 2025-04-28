package services;

import dtos.UserRequestDto;
import entities.Users;

import repositories.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public void createUser(UserRequestDto  requestDto) {
        Users user = new Users();
        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());
        user.setCpf(requestDto.getCpf());
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
