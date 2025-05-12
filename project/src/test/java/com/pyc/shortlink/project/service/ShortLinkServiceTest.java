package com.pyc.shortlink.project.service;

import com.pyc.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.pyc.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.pyc.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.pyc.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class ShortLinkServiceTest {

    @Mock
    private ShortLinkService shortLinkService;

    @Test
    public void testCreateShortLink_InvalidRequestParam() {
        ShortLinkCreateReqDTO requestParam = null;
        Mockito.when(shortLinkService.createShortLink(requestParam)).thenThrow(new IllegalArgumentException("RequestParam cannot be null"));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            shortLinkService.createShortLink(requestParam);
        });
        assertEquals("RequestParam cannot be null", exception.getMessage());
    }

    @Test
    public void testCreateShortLink_EmptyUrl() {
        ShortLinkCreateReqDTO requestParam = new ShortLinkCreateReqDTO();
        requestParam.setOriginalUrl("");
        Mockito.when(shortLinkService.createShortLink(requestParam)).thenThrow(new IllegalArgumentException("Original URL cannot be empty"));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            shortLinkService.createShortLink(requestParam);
        });
        assertEquals("Original URL cannot be empty", exception.getMessage());
    }

    @Test
    public void testCreateShortLink_MaxUrlLength() {
        ShortLinkCreateReqDTO requestParam = new ShortLinkCreateReqDTO();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2048; i++) {
            sb.append("a");
        }
        requestParam.setOriginalUrl(sb.toString());
        ShortLinkCreateRespDTO response = shortLinkService.createShortLink(requestParam);
        assertNotNull(response);
    }

    @Test
    public void testCreateShortLink_UrlLengthExceedsMax() {
        ShortLinkCreateReqDTO requestParam = new ShortLinkCreateReqDTO();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2049; i++) {
            sb.append("a");
        }
        requestParam.setOriginalUrl(sb.toString());
        Mockito.when(shortLinkService.createShortLink(requestParam)).thenThrow(new IllegalArgumentException("URL length exceeds maximum allowed"));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            shortLinkService.createShortLink(requestParam);
        });
        assertEquals("URL length exceeds maximum allowed", exception.getMessage());
    }

    @Test
    public void testCreateShortLink_MinExpiration() {
        ShortLinkCreateReqDTO requestParam = new ShortLinkCreateReqDTO();
        requestParam.setExpirationDays(1);
        ShortLinkCreateRespDTO response = shortLinkService.createShortLink(requestParam);
        assertNotNull(response);
    }

    @Test
    public void testCreateShortLink_ExpirationLessThanMin() {
        ShortLinkCreateReqDTO requestParam = new ShortLinkCreateReqDTO();
        requestParam.setExpirationDays(0);
        Mockito.when(shortLinkService.createShortLink(requestParam)).thenThrow(new IllegalArgumentException("Expiration days cannot be less than 1"));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            shortLinkService.createShortLink(requestParam);
        });
        assertEquals("Expiration days cannot be less than 1", exception.getMessage());
    }

    @Test
    public void testBatchCreateShortLink_EmptyRequestParam() {
        ShortLinkBatchCreateReqDTO requestParam = null;
        Mockito.when(shortLinkService.batchCreateShortLink(requestParam)).thenThrow(new IllegalArgumentException("RequestParam cannot be null"));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            shortLinkService.batchCreateShortLink(requestParam);
        });
        assertEquals("RequestParam cannot be null", exception.getMessage());
    }

    @Test
    public void testBatchCreateShortLink_EmptyLinkList() {
        ShortLinkBatchCreateReqDTO requestParam = new ShortLinkBatchCreateReqDTO();
        requestParam.setLinks(null);
        Mockito.when(shortLinkService.batchCreateShortLink(requestParam)).thenThrow(new IllegalArgumentException("Link list cannot be empty"));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            shortLinkService.batchCreateShortLink(requestParam);
        });
        assertEquals("Link list cannot be empty", exception.getMessage());
    }

    @Test
    public void testBatchCreateShortLink_MinLinkCount() {
        ShortLinkBatchCreateReqDTO requestParam = new ShortLinkBatchCreateReqDTO();
        requestParam.setLinks(List.of(new ShortLinkCreateReqDTO()));
        ShortLinkBatchCreateRespDTO response = shortLinkService.batchCreateShortLink(requestParam);
        assertNotNull(response);
    }

    @Test
    public void testBatchCreateShortLink_MaxLinkCount() {
        ShortLinkBatchCreateReqDTO requestParam = new ShortLinkBatchCreateReqDTO();
        List<ShortLinkCreateReqDTO> links = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            links.add(new ShortLinkCreateReqDTO());
        }
        requestParam.setLinks(links);
        ShortLinkBatchCreateRespDTO response = shortLinkService.batchCreateShortLink(requestParam);
        assertNotNull(response);
    }

    @Test
    public void testBatchCreateShortLink_LinkCountExceedsMax() {
        ShortLinkBatchCreateReqDTO requestParam = new ShortLinkBatchCreateReqDTO();
        List<ShortLinkCreateReqDTO> links = new ArrayList<>();
        for (int i = 0; i < 1001; i++) {
            links.add(new ShortLinkCreateReqDTO());
        }
        requestParam.setLinks(links);
        Mockito.when(shortLinkService.batchCreateShortLink(requestParam)).thenThrow(new IllegalArgumentException("Link count exceeds maximum allowed"));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            shortLinkService.batchCreateShortLink(requestParam);
        });
        assertEquals("Link count exceeds maximum allowed", exception.getMessage());
    }

//    @org.junit.jupiter.api.Test
//    void createShortLink() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void createShortLinkByLock() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void batchCreateShortLink() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void updateShortLink() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void pageShortLink() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void listGroupShortLinkCount() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void restoreUrl() {
//    }
//
//    @org.junit.jupiter.api.Test
//    void shortLinkStats() {
//    }
}