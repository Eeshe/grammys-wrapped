package me.eeshe.grammyswrapped.service;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalizationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(LocalizationService.class);
  private static final String BUNDLE_BASE_NAME = "localization.messages";

  private static LocalizationService instance;

  private final ResourceBundle messagesBundle;
  private final Locale currentLocale;

  private LocalizationService(Locale locale) {
    this.currentLocale = locale;
    ResourceBundle loadedBundle;
    try {
      loadedBundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
    } catch (MissingResourceException e) {
      LOGGER.error("Couldn't find resource bundle for locale '{}'. Falling back to default bundle.", locale);
      loadedBundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME);
    }
    this.messagesBundle = loadedBundle;
  }

  /**
   * Initializes the class' singleton.
   *
   * @param languageCode LanguageCode to use in the localization.
   */
  public static void initialize(String languageCode) {
    if (instance != null) {
      LOGGER.error("LocalizationService is already initialized. Restart the bot to change localization.");
      return;
    }
    instance = new LocalizationService(Locale.of(languageCode));
  }

  public static LocalizationService getInstance() {
    if (instance == null) {
      LOGGER.error("LocalizationService hasn't been initialized yet.");
    }
    return instance;
  }

  /**
   * Retrieves a localized string for the given key.
   * If the key is not found in the current bundle (or its fallbacks), it prints a
   * warning and returns the key itself as a placeholder.
   *
   * @param key The key of the message to retrieve.
   * @return The localized string, or the key itself if not found.
   */
  public String getString(String key) {
    try {
      return messagesBundle.getString(key);
    } catch (MissingResourceException e) {
      LOGGER.error("Warning: Missing translation key '{}' for locale {}. Returning key itself.");
      return key;
    }
  }

  /**
   * Retrieves a localized string and formats it with the given arguments.
   * Uses String.format() for placeholder replacement.
   *
   * @param key  The key of the message to retrieve.
   * @param args Arguments to format into the message string.
   * @return The formatted localized string.
   */
  public String getFormattedString(String key, Object... args) {
    String pattern = getString(key);

    return String.format(pattern, args);
  }

  /**
   * Returns the Locale currently used by this manager.
   *
   * @return The current Locale.
   */
  public Locale getCurrentLocale() {
    return currentLocale;
  }
}
