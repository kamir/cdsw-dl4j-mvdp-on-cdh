package io.filefilters;

import java.io.*;

public class PNGFileFilter implements FileFilter
    {
        private final String[] okFileExtensions = new String[] { "png" };

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

