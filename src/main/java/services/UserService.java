package services;

import entities.Users;
import repositories.GenericRepository;

public class UserService {
   private final GenericRepository<Users, Long> genericRepository;

    public UserService(GenericRepository<Users, Long> genericRepository) {
        this.genericRepository = genericRepository;
    }

    public void createUser(String name, String email, String cpf) {
        Users user = new Users();
        user.setId(System.currentTimeMillis()); // Exemplo: gera ID automático
        user.setName(name);
        user.setEmail(email);
        user.setCpf(cpf);
        genericRepository.save(user);
    }
}
