/*
 * Copyright (C) 2010 Vivien Barousse
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.aperigeek.latitude.plotter;

import com.aperigeek.geo.GeoLocation;
import com.aperigeek.geo.image.plotter.BoundingBox;
import com.aperigeek.geo.image.plotter.SequentialImagePlotter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Vivien Barousse
 */
public class Main {
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar plotter.jar kml image");
        }
        
        try {
            File file = new File(args[0]);
            KmlHandler handler = new KmlHandler();
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(file, handler);
            
            SequentialImagePlotter plotter = new SequentialImagePlotter(1600, 1200);
            plotter.setBoundingBox(new BoundingBox(85, -85, 180, -180));
            BufferedImage image = plotter.plot(handler.locations, null);
            System.out.println(handler.locations);
            
            ImageIO.write(image, "png", new File(args[1]));
            
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static class KmlHandler extends DefaultHandler {
        
        private static final Pattern coordinatesPattern = 
                Pattern.compile("(-?[0-9\\.]+),(-?[0-9\\.]+),[0-9]+");
        
        private boolean inCoordinates = false;
        
        private List<GeoLocation> locations = new ArrayList<GeoLocation>();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("coordinates")) {
                inCoordinates = true;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String str = new String(ch, start, length);
            if (inCoordinates) {
                Matcher matcher = coordinatesPattern.matcher(str);
                if (matcher.matches()) {
                    locations.add(new GeoLocation(
                            Double.parseDouble(matcher.group(2)), 
                            Double.parseDouble(matcher.group(1))));
                } else {
                    System.out.println("Unexpected content " + str);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("coordinates")) {
                inCoordinates = false;
            }
        }
        
    }
    
}
