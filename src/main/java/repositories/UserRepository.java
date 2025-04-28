package repositories;

import entities.Users;
import configurations.genericsRepositories.GenericRepositoryImpl;

public class UserRepository extends GenericRepositoryImpl<Users, Long> {


    public UserRepository() {
        super(Users.class);
    }
}
