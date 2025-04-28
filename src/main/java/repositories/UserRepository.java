package repositories;

import entities.Users;

public class UserRepository extends GenericRepositoryImpl<Users, Long> {


    public UserRepository() {
        super(Users.class);
    }
}
