package io.filefilters;

import java.io.File;
import java.io.FileFilter;

public class ModelFileFilter implements FileFilter
    {

        String PATTERN = "."; // allways is part of a .zip filename

        public ModelFileFilter( String pattern ) {
            PATTERN = pattern + "_";
        }

        private final String[] okFileExtensions = new String[] { "zip" };

        public boolean accept(File file)
        {
            for (String extension : okFileExtensions)
            {
                if (file.getName().toLowerCase().endsWith(extension))
                {
                    if ( file.getName().contains( PATTERN ) )
                        return true;
                }
            }
            return false;
        }
    }

