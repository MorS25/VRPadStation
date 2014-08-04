package com.laser.parameters;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.laser.helpers.file.DirectoryPath;

import java.io.*;
import java.util.Map;


/**
 * Parameter metadata parser extracted from parameters
 */
public class ParameterMetadataMapReader {

    private static final String PARAMETER_METADATA_PATH = "Parameters/ParameterMetaDataBackup.xml";
    private static final String METADATA_DISPLAYNAME = "DisplayName";
    private static final String METADATA_DESCRIPTION = "Description";
    private static final String METADATA_UNITS = "Units";
    private static final String METADATA_VALUES = "Values";
    private static final String METADATA_RANGE = "Range";


    public static Map<String, ParameterMetadata> load(Context context, String metadataType) throws IOException, XmlPullParserException 
    {
        final InputStream inputStream;
        final File file = new File(DirectoryPath.getVrPadStationPath() + PARAMETER_METADATA_PATH);
        if(file.exists()) {
            inputStream = new FileInputStream(file);
        } else {
            inputStream = context.getAssets().open(PARAMETER_METADATA_PATH);
        }
        return open(inputStream, metadataType);
    }

    private static ParameterMetadataMap open(InputStream inputStream, String metadataType) throws XmlPullParserException, IOException 
    {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            return parseMetadata(parser, metadataType);
        } finally {
            try { inputStream.close(); } catch (IOException ex) {}
        }
    }

    private static void addMetaDataProperty(ParameterMetadata metaData, String name, String text) {
        if(name.equals(METADATA_DISPLAYNAME))
            metaData.setDisplayName(text);
        else if(name.equals(METADATA_DESCRIPTION))
            metaData.setDescription(text);
        else if(name.equals(METADATA_UNITS))
            metaData.setUnits(text);
        else if(name.equals(METADATA_RANGE))
            metaData.setRange(text);
        else if(name.equals(METADATA_VALUES))
            metaData.setValues(text);
    }

    private static ParameterMetadataMap parseMetadata(XmlPullParser parser, String type) throws XmlPullParserException, IOException {
        String name;
        boolean parsing = false;
        ParameterMetadata parameterMetadata = null;
        ParameterMetadataMap parameterMetadataMap = new ParameterMetadataMap();

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) 
        {
            switch (eventType) 
            {
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if(type.equals(name)) {
                        parsing = true;
                    } else if(parsing) {
                        if(parameterMetadata == null) {
                        	parameterMetadata = new ParameterMetadata();
                        	parameterMetadata.setName(name);
                        } else {
                            addMetaDataProperty(parameterMetadata, name, parser.nextText());
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if(type.equals(name)) {
                        return parameterMetadataMap;
                    } else if(parameterMetadata != null && parameterMetadata.getName().equals(name)) {
                    	parameterMetadataMap.put(parameterMetadata.getName(), parameterMetadata);
                    	parameterMetadata = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
        return null;
    }

}
