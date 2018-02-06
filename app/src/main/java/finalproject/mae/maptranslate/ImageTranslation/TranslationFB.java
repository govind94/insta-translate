package finalproject.mae.maptranslate.ImageTranslation;
import android.net.Uri;

public class TranslationFB {
    String targetLanguage;
    Double latitude;
    Double longitude;
    String imageName;
    String translatedText;

    public TranslationFB() {
    }

    public TranslationFB(String targetLanguage, Double latitude, Double longitude, String imageName, String translatedText) {
        this.targetLanguage = targetLanguage;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageName = imageName;
        this.translatedText = translatedText;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String imageName() {
        return imageName;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void putImageName(String imageName) {
        this.imageName = imageName;
    }
}
