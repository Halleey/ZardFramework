package project.repositories;

import configurations.genericsRepositories.GenericRepository;
import configurations.instancias.Repository;
import entities.Address;

@Repository
public interface AddressRepository extends GenericRepository<Address, Long> {
}
