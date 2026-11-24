package com.sharesphere.borrowcircle.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/borrowcircle/listings/images")
public class ListingImageController {
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private final Path storageDirectory;
    private final RestClient communities;
    private final String internalToken;

    public ListingImageController(
            @Value("${sharesphere.upload.dir:/data/uploads}") String uploadDirectory,
            @Value("${services.community:http://localhost:8082}") String communityUrl,
            @Value("${sharesphere.internal.token}") String internalToken) {
        this.storageDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
        this.communities = RestClient.builder().baseUrl(communityUrl).build();
        this.internalToken = internalToken;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageUploadView upload(
            @RequestParam UUID communityId,
            @RequestPart("file") MultipartFile file,
            @RequestHeader("X-User-Email") String email) {
        requireMember(communityId, email);
        if (file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an image to upload");
        if (file.getSize() > MAX_IMAGE_BYTES) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Images must be 5 MB or smaller");

        String extension;
        MediaType mediaType;
        if (MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(file.getContentType())) {
            extension = "jpg";
            mediaType = MediaType.IMAGE_JPEG;
        } else if (MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(file.getContentType())) {
            extension = "png";
            mediaType = MediaType.IMAGE_PNG;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a JPEG or PNG image");
        }

        try (InputStream input = file.getInputStream()) {
            if (ImageIO.read(input) == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected file is not a valid image");
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected file is not a valid image", exception);
        }

        String filename = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(storageDirectory);
            file.transferTo(storageDirectory.resolve(filename));
            return new ImageUploadView("/api/borrowcircle/listings/images/" + filename, mediaType.toString());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image could not be saved", exception);
        }
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> get(@PathVariable String filename) {
        if (!filename.matches("[0-9a-fA-F-]{36}\\.(jpg|png)")) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        Path image = storageDirectory.resolve(filename).normalize();
        if (!image.startsWith(storageDirectory) || !Files.isRegularFile(image)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        MediaType mediaType = filename.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(mediaType).cacheControl(CacheControl.noCache()).body(new FileSystemResource(image));
    }

    private void requireMember(UUID communityId, String email) {
        try {
            Boolean member = communities.get()
                    .uri("/api/communities/internal/{id}/members/{email}", communityId, email)
                    .header("X-Internal-Token", internalToken)
                    .retrieve().body(Boolean.class);
            if (!Boolean.TRUE.equals(member)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You must belong to the listing community");
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Community membership could not be verified", exception);
        }
    }

    public record ImageUploadView(String imageUrl, String mediaType) {}
}
