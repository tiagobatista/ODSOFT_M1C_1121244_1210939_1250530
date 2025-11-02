package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.SQL;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import pt.psoft.g1.psoftg1.shared.infrastructure.repositories.impl.Mapper.PhotoEntityMapper;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

/**
 * SQL implementation of PhotoRepository
 * Delegates operations to SpringDataPhotoRepository
 */
@Profile("sql-redis")
@Primary
@Repository
@RequiredArgsConstructor
public class PhotoRepositoryImpl implements PhotoRepository {

    private final SpringDataPhotoRepository springDataPhotoRepo;
    private final PhotoEntityMapper mapper;

    @Override
    public void deleteByPhotoFile(String photoFile) {
        springDataPhotoRepo.deleteByPhotoFile(photoFile);
    }
}
