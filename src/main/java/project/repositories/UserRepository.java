package project.repositories;

import configurations.dbas.Querys;
import configurations.genericsRepositories.GenericRepository;
import entities.Users;

import java.util.List;


public interface UserRepository extends GenericRepository<Users, Long> {


    @Querys("SELECT * FROM users WHERE address_id = ?")
    List<Users> findUsersByAddressId(Long address_Id);

    @Querys("SELECT * FROM users WHERE name = ?")
    List<Users> findUsersByName(String name);
}
