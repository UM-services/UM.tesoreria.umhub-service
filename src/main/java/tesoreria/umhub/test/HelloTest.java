package tesoreria.umhub.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tesoreria/umhub/hello")
public class HelloTest {

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("hello");
    }
}
