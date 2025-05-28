package project.repositories;

import configurations.genericsRepositories.GenericRepository;
import configurations.instancias.Repository;
import project.entities.Address;

@Repository
public interface AddressRepository extends GenericRepository<Address, Long> {
}
