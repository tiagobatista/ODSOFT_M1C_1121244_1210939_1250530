package pt.psoft.g1.psoftg1.lendingmanagement.model.SQL;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Entity
@Table(name = "FINE")
@Profile("sql-redis")
@Primary
@Getter
@Setter
public class FineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lending_fk", unique = true)
    private LendingEntity relatedLending;

    @Column(nullable = false)
    private Integer dailyRate;

    @Column(nullable = false)
    private Integer totalAmount;

    // Construtor vazio para JPA
    protected FineEntity() {}

    // Construtor principal
    public FineEntity(LendingEntity relatedLending, Integer dailyRate, Integer totalAmount) {
        this.relatedLending = relatedLending;
        this.dailyRate = dailyRate;
        this.totalAmount = totalAmount;
    }
}
