package choo.weather.repository;

import choo.weather.domain.Memo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class JdcMemoRepositoryTest {

    @Autowired
    JdcMemoRepository jdcMemoRepository;


    @Test
    void insertNewMemo() {
        //given
        Memo newMemo = new Memo(2, "이건 2번이야");
        //when
        jdcMemoRepository.save(newMemo);
        //then
        Optional<Memo> result = jdcMemoRepository.findById(2);
        assertEquals(result.get().getText() ,"이건 2번이야");

    }

}