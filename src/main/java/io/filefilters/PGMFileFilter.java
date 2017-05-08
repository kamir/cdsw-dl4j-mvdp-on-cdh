package io.filefilters;

import java.io.File;
import java.io.FileFilter;

public class PGMFileFilter implements FileFilter
    {
        private final String[] okFileExtensions = new String[] { "pgm" };

        public boolean accept(File file)
        {
            for (String extension : okFileExtensions)
            {
                if (file.getName().toLowerCase().endsWith(extension))
                {
                    return true;
                }
            }
            return false;
        }
    }

