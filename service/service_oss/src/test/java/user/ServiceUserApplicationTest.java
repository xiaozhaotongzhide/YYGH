package user;

import com.example.yygh.oss.confing.qiNiuYunConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;



@SpringBootTest
@Slf4j
public class ServiceUserApplicationTest {


    @Test
    public void detectionProfile() {
        log.info(qiNiuYunConfig.ACCESS_KET);
        log.info(qiNiuYunConfig.SECRET_KET);
        log.info(qiNiuYunConfig.BUCKET);
    }

}
