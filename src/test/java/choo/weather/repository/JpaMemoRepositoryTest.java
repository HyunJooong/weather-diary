package choo.weather.repository;

import choo.weather.domain.Memo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class JpaMemoRepositoryTest {

    @Autowired
    JpaMemoRepository jpaMemoRepository;

    @BeforeEach
    void setUp() {
        jpaMemoRepository.deleteAll(); // 모든 데이터를 삭제하여 테스트 데이터 격리
    }


    @Test
    void insertMemoTet() {
        //given
        Memo newMemo = new Memo(null, "this is memo");
        //when
        Memo saveMemo = jpaMemoRepository.save(newMemo);
        //then
        List<Memo> memoList = jpaMemoRepository.findAll();
        assertNotNull(saveMemo.getId());
        //assertTrue(memoList.size() > 0);
    }


    @Test
    void findByIdTest() {
        //given
        Memo newMemo = new Memo(null, "this is retry");
        //when
        Memo memo = jpaMemoRepository.save(newMemo);
        //System.out.println(memo.getId());
        //then
        Optional<Memo> result = jpaMemoRepository.findById(memo.getId());
        assertEquals(result.get().getText(), "this is retry");
    }
}