package com.auscultator.data;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by hongyan on 2014/10/23.
 */
public class SAX_AudioTagService {
    private boolean isParse = false;
    private AudioTag heartSounds;
    private AudioTag breathSounds;

    public boolean parse(InputStream inputStream) throws Throwable {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        AudioTagHandler handler = new AudioTagHandler();
        parser.parse(inputStream, handler);

        heartSounds = handler.getHeartSounds();
        breathSounds = handler.getBreathSounds();

        isParse = true;
        return true;
    }

    public List<AudioTag> getHeartSounds() {
        return isParse ? heartSounds.getChildren() : null;
    }

    public List<AudioTag> getBreathSounds() {
        return isParse ? breathSounds.getChildren() : null;
    }

    private class AudioTagHandler extends DefaultHandler {
        private String TAG_SOUNDS = "sounds";
        private String ATTR_SOUNDS_TYPE = "type";
        private String ATTR_SOUNDS_TYPE_HEART = "heart";
        private String ATTR_SOUNDS_TYPE_BREATH = "breath";
        private String TAG_DIRECTORY = "directory";
        private String TAG_AUDIO = "audio";
        private String ATTR_NAME = "name";
        private String ATTR_PATH = "path";
        private String ATTR_TAG = "tag";

        private AudioTag heartSounds;         // The list of the heart sounds
        private AudioTag breathSounds;        // The list of the breath sounds
        private AudioTag currentDirectory;               // The current sound directory
        private AudioTag newNode;

        private boolean isDir;
        private String name;
        private String label;
        private String path;

        public AudioTag getHeartSounds() {
            return heartSounds;
        }

        public AudioTag getBreathSounds() {
            return breathSounds;
        }

        @Override
        public void startDocument() throws SAXException {
            heartSounds = new AudioTag(true, ATTR_SOUNDS_TYPE_HEART, ATTR_SOUNDS_TYPE_HEART, "./");
            breathSounds = new AudioTag(true, ATTR_SOUNDS_TYPE_BREATH, ATTR_SOUNDS_TYPE_BREATH, "./");
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            // Audio
            if (localName.equals(TAG_AUDIO)) {
                isDir = false;
                name = attributes.getValue(ATTR_NAME);
                label = attributes.getValue(ATTR_TAG);
                path = attributes.getValue(ATTR_PATH);

                newNode = new AudioTag(isDir, name, label, path);
                currentDirectory.push(newNode);
            }
            // Directory
            else if (localName.equals(TAG_DIRECTORY)) {
                isDir = true;
                name = attributes.getValue(ATTR_NAME);
                label = attributes.getValue(ATTR_TAG);
                path = attributes.getValue(ATTR_PATH);

                newNode = new AudioTag(isDir, name, label, path);
                currentDirectory.push(newNode);

                currentDirectory = newNode;
            }
            // Sounds Type
            else if (localName.equals(TAG_SOUNDS)) {
                if (attributes.getValue(ATTR_SOUNDS_TYPE).equals(ATTR_SOUNDS_TYPE_BREATH)) {
                    currentDirectory = breathSounds;
                } else if(attributes.getValue(ATTR_SOUNDS_TYPE).equals(ATTR_SOUNDS_TYPE_HEART) ||
                        attributes.getValue(ATTR_SOUNDS_TYPE) == null) {
                    currentDirectory = heartSounds;
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (localName.equals(TAG_DIRECTORY)) {
                currentDirectory = currentDirectory.getParent();
            }
        }
    }
}


