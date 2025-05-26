package project.repositories;

import configurations.dbas.Querys;
import configurations.genericsRepositories.GenericRepository;
import configurations.instancias.Repository;
import entities.Users;
import project.dtos.UserResponseDTO;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends GenericRepository<Users, Long> {


    @Querys("SELECT * FROM users WHERE address_id = ?")
    List<Users> findUsersByAddressId(Long address_Id);

    @Querys("SELECT name, address_id FROM frame.users where name = ?")
    List<UserResponseDTO> findUsersByName(String name);

    @Querys("SELECT * FROM users WHERE name = ?")
    Users findByName(String name);


}
