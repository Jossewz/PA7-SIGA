package com.siga.siga_iea.storage;

import com.siga.siga_iea.storage.dto.FileMetadata;
import com.siga.siga_iea.storage.dto.UploadResult;
import com.siga.siga_iea.storage.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private StorageProvider provider;

    private StorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.setUrl("http://localhost:9000");
        properties.setAccessKey("admin");
        properties.setSecretKey("admin123");
        properties.setBucketName("siga");

        storageService = new StorageServiceImpl(provider, properties);
    }

    // ====================== Upload Tests ======================

    @Test
    @DisplayName("upload() should generate a UUID filename, not use the original")
    void upload_generatesUuidFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "foto", "mi foto bonita.jpg", "image/jpeg", "fake-image-data".getBytes()
        );

        UploadResult result = storageService.upload(file, StorageFolder.ESTUDIANTES, "15");

        assertNotNull(result.getKey());
        assertTrue(result.getKey().startsWith("estudiantes/15/"));
        assertTrue(result.getKey().endsWith(".jpg"));
        // The key should NOT contain the original filename
        assertFalse(result.getKey().contains("mi foto bonita"));
    }

    @Test
    @DisplayName("upload() should delegate to StorageProvider.putObject()")
    void upload_delegatesToProvider() {
        MockMultipartFile file = new MockMultipartFile(
                "doc", "cedula.pdf", "application/pdf", "pdf-content".getBytes()
        );

        storageService.upload(file, StorageFolder.MATRICULAS, "42");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(provider, times(1)).putObject(
                keyCaptor.capture(),
                any(InputStream.class),
                eq((long) "pdf-content".getBytes().length),
                eq("application/pdf")
        );

        String capturedKey = keyCaptor.getValue();
        assertTrue(capturedKey.startsWith("matriculas/42/"));
        assertTrue(capturedKey.endsWith(".pdf"));
    }

    @Test
    @DisplayName("upload() should return correct metadata")
    void upload_returnsCorrectMetadata() {
        MockMultipartFile file = new MockMultipartFile(
                "foto", "profile.png", "image/png", new byte[1024]
        );

        UploadResult result = storageService.upload(file, StorageFolder.DOCENTES, "8");

        assertNotNull(result.getMetadata());
        assertEquals("profile.png", result.getMetadata().getOriginalFilename());
        assertEquals("image/png", result.getMetadata().getContentType());
        assertEquals(1024, result.getMetadata().getSize());
        assertEquals("siga", result.getMetadata().getBucket());
        assertNotNull(result.getMetadata().getUploadDate());
    }

    @Test
    @DisplayName("upload() should throw StorageException for empty files")
    void upload_throwsOnEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]
        );

        assertThrows(StorageException.class, () ->
                storageService.upload(emptyFile, StorageFolder.ESTUDIANTES, "1")
        );
    }

    // ====================== Download Tests ======================

    @Test
    @DisplayName("download() should return the InputStream from the provider")
    void download_returnsInputStream() {
        InputStream expected = new ByteArrayInputStream("file-data".getBytes());
        when(provider.getObject("estudiantes/15/abc.jpg")).thenReturn(expected);

        InputStream result = storageService.download("estudiantes/15/abc.jpg");

        assertSame(expected, result);
        verify(provider).getObject("estudiantes/15/abc.jpg");
    }

    // ====================== Delete Tests ======================

    @Test
    @DisplayName("delete() should call StorageProvider.removeObject()")
    void delete_callsProviderRemove() {
        storageService.delete("estudiantes/15/abc.jpg");

        verify(provider, times(1)).removeObject("estudiantes/15/abc.jpg");
    }

    // ====================== Exists Tests ======================

    @Test
    @DisplayName("exists() should delegate to StorageProvider.objectExists()")
    void exists_delegatesToProvider() {
        when(provider.objectExists("docentes/8/cv.pdf")).thenReturn(true);

        assertTrue(storageService.exists("docentes/8/cv.pdf"));
        verify(provider).objectExists("docentes/8/cv.pdf");
    }

    @Test
    @DisplayName("exists() should return false when object does not exist")
    void exists_returnsFalseWhenMissing() {
        when(provider.objectExists("docentes/99/nope.pdf")).thenReturn(false);

        assertFalse(storageService.exists("docentes/99/nope.pdf"));
    }

    // ====================== Move Tests ======================

    @Test
    @DisplayName("move() should copy then delete the source")
    void move_copiesAndDeletes() {
        String source = "temp/abc.pdf";
        String dest = "estudiantes/15/documento.pdf";

        String result = storageService.move(source, dest);

        assertEquals(dest, result);
        verify(provider).copyObject(source, dest);
        verify(provider).removeObject(source);
    }

    // ====================== List Tests ======================

    @Test
    @DisplayName("list() should return FileMetadata for each object under the prefix")
    void list_returnsMetadataList() {
        when(provider.listObjects("estudiantes/15/"))
                .thenReturn(List.of("estudiantes/15/foto.jpg", "estudiantes/15/doc.pdf"));

        List<FileMetadata> result = storageService.list(StorageFolder.ESTUDIANTES, "15");

        assertEquals(2, result.size());
        assertEquals("estudiantes/15/foto.jpg", result.get(0).getKey());
        assertEquals("estudiantes/15/doc.pdf", result.get(1).getKey());
    }

    // ====================== Validation Tests ======================

    @Test
    @DisplayName("validateSize() should throw when file exceeds max size")
    void validateSize_throwsOnOversize() {
        MockMultipartFile bigFile = new MockMultipartFile(
                "file", "big.pdf", "application/pdf", new byte[6_000_000] // 6MB
        );

        assertThrows(StorageException.class, () ->
                storageService.validateSize(bigFile, 5_000_000) // max 5MB
        );
    }

    @Test
    @DisplayName("validateSize() should pass when file is within limit")
    void validateSize_passesWhenWithinLimit() {
        MockMultipartFile smallFile = new MockMultipartFile(
                "file", "small.pdf", "application/pdf", new byte[1_000]
        );

        assertDoesNotThrow(() ->
                storageService.validateSize(smallFile, 5_000_000)
        );
    }

    @Test
    @DisplayName("validateExtension() should throw for disallowed extensions")
    void validateExtension_throwsOnInvalid() {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file", "virus.exe", "application/octet-stream", "data".getBytes()
        );

        assertThrows(StorageException.class, () ->
                storageService.validateExtension(exeFile, "jpg", "png", "pdf")
        );
    }

    @Test
    @DisplayName("validateExtension() should pass for allowed extensions")
    void validateExtension_passesForValid() {
        MockMultipartFile jpgFile = new MockMultipartFile(
                "file", "photo.JPG", "image/jpeg", "data".getBytes()
        );

        assertDoesNotThrow(() ->
                storageService.validateExtension(jpgFile, "jpg", "png", "pdf")
        );
    }
}
