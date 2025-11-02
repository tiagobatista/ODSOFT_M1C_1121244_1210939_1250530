package pt.psoft.g1.psoftg1.readermanagement.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.model.SQL.ReaderDetailsEntity;


@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReaderBookCountDTO {
    private ReaderDetailsEntity readerDetails;
    private long lendingCount;
}
