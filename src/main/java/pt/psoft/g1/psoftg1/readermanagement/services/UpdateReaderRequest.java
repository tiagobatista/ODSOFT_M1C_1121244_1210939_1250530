package pt.psoft.g1.psoftg1.readermanagement.services;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
public class UpdateReaderRequest {

    @Nullable
    private String fullName;

    @Nullable
    private String email;

    @Nullable
    private String birthDate;

    @Nullable
    private String phoneNumber;

    @Getter(lombok.AccessLevel.NONE) // Desabilita o getter automático do Lombok
    private boolean marketing;

    @Getter(lombok.AccessLevel.NONE) // Desabilita o getter automático do Lombok
    private boolean thirdParty;

    @Nullable
    private String username;

    @Nullable
    private String password;

    @Nullable
    private MultipartFile photo;

    @Nullable
    private List<String> interestList;

    // Getters manuais com 'get' em vez de 'is'
    public boolean getMarketing() {
        return marketing;
    }

    public boolean getThirdParty() {
        return thirdParty;
    }

    // Setters manuais
    public void setMarketing(boolean marketing) {
        this.marketing = marketing;
    }

    public void setThirdParty(boolean thirdParty) {
        this.thirdParty = thirdParty;
    }
}