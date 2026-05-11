package com.replyiq.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class SeoPageController {

    @GetMapping(value = "/for/{slug}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> categoryPage(@PathVariable String slug) {
        return serveSeoPage("for/" + slug);
    }

    @GetMapping(value = "/for/{category}/in/{city}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> comboCategoryCity(@PathVariable String category, @PathVariable String city) {
        return serveSeoPage("for/" + category + "/in/" + city);
    }

    @GetMapping(value = "/in/{slug}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> cityPage(@PathVariable String slug) {
        return serveSeoPage("in/" + slug);
    }

    @GetMapping(value = "/vs/{slug}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> comparisonPage(@PathVariable String slug) {
        return serveSeoPage("vs/" + slug);
    }

    @GetMapping(value = "/how-to/{slug}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> howToPage(@PathVariable String slug) {
        return serveSeoPage("how-to/" + slug);
    }

    private ResponseEntity<String> serveSeoPage(String path) {
        try {
            Resource resource = new ClassPathResource("static/" + path + "/index.html");
            if (resource.exists()) {
                String html = resource.getContentAsString(StandardCharsets.UTF_8);
                return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
            }
        } catch (IOException ignored) {}

        return ResponseEntity.notFound().build();
    }
}
