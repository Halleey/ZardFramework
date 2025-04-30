package entities;

import configurations.dbas.Column;
import configurations.dbas.Entity;
import configurations.dbas.Id;
import configurations.dbas.OneToOne;

@Entity
public class Banana {
    @Id
    Long id;
    @Column
    String saliencia;

    @OneToOne
    private macaco macaco;
}
