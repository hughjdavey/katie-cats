package com.hugh.katiecats.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ImgurUtil {

    final static private String API_GALLERY_PATH = "gallery/r/catpictures/top/";
    final static private String API_HTTP_ENDPOINT = "https://api.imgur.com/3/";
    final static private String API_CLIENT_ID = "YOUR_IMGUR_CLIENT_ID";
    final static private String CAT_DIRECTORY_NAME = "My Best Cats";
    final static private String HTTP_GET = "GET";
    final static private String IMAGE_EXTENSION = ".jpg";
    final static private String IMGUR_BASE_URL = "http://i.imgur.com/";
    final static private String LOGTAG = "Imgur Client";
    final static private String XML_EXTENSION = ".xml";

    public static List<Uri> getSavedCats() {
        File catDir = getAlbumStorageDir(CAT_DIRECTORY_NAME);
        String[] allFiles = catDir.list();       // list of strings of filenames e.g. cat_22.png

        List<Uri> savedCats = new ArrayList<>();
        if (allFiles != null) {
            for (String allFile : allFiles) {
                File f = new File(catDir, allFile);
                savedCats.add(Uri.fromFile(f));
            }

            // natural sort to sort the filename strings based on their numeric values
            // e.g. a natural sort puts 'file22' after 'file3'
            // sourced from http://stackoverflow.com/questions/7270447/java-string-number-comparator
            Collections.sort(savedCats, new Comparator<Uri>() {
                @Override
                public int compare(Uri lhs, Uri rhs) {
                    String a = rhs.getPath();
                    String b = lhs.getPath();
                    int la = a.length();
                    int lb = b.length();
                    int ka = 0;
                    int kb = 0;
                    while (true) {
                        if (ka == la)
                            return kb == lb ? 0 : -1;
                        if (kb == lb)
                            return 1;
                        if (a.charAt(ka) >= '0' && a.charAt(ka) <= '9' && b.charAt(kb) >= '0' && b.charAt(kb) <= '9') {
                            int na = 0;
                            int nb = 0;
                            while (ka < la && a.charAt(ka) == '0')
                                ka++;
                            while (ka + na < la && a.charAt(ka + na) >= '0' && a.charAt(ka + na) <= '9')
                                na++;
                            while (kb < lb && b.charAt(kb) == '0')
                                kb++;
                            while (kb + nb < lb && b.charAt(kb + nb) >= '0' && b.charAt(kb + nb) <= '9')
                                nb++;
                            if (na > nb)
                                return 1;
                            if (nb > na)
                                return -1;
                            if (ka == la)
                                return kb == lb ? 0 : -1;
                            if (kb == lb)
                                return 1;

                        }
                        if (a.charAt(ka) != b.charAt(kb))
                            return a.charAt(ka) - b.charAt(kb);
                        ka++;
                        kb++;
                    }
                }
            });
        }
        return savedCats;
    }

    public static Uri getSavedCatUri() {
        File directory = getAlbumStorageDir(CAT_DIRECTORY_NAME);
        File firstCat = new File(directory, indexToFilename(1));
        return Uri.fromFile(firstCat);
    }

    /* converts array index into the filename contained in that position in the array */
    private static String indexToFilename(int index) {
        return "cat_" + index + ".png";
    }

    public enum IMAGE_SIZE {
        SMALL("t"), MEDIUM("m"), LARGE("l");
        public String suffix;

        IMAGE_SIZE(String suffix) {
            this.suffix = suffix;
        }
    }

    public static HashSet<String> getImageUrls(IMAGE_SIZE imageSize) {
        return getImageUrls(imageSize, 0);
    }

    public static HashSet<String> getImageUrls(IMAGE_SIZE imageSize, int pageNumber) {
        HashSet<String> imageUrls = new HashSet<>();
        for (String imageHash : getImageHashes(pageNumber)) {
            imageUrls.add(getImgurUrl(imageHash, imageSize));
        }
        return imageUrls;
    }

    private static HashSet<String> getImageHashes(int pageNumber) {
        HashSet<String> hashList = new HashSet<>();
        String apiUrl = API_HTTP_ENDPOINT + API_GALLERY_PATH + pageNumber + XML_EXTENSION;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Client-ID " + API_CLIENT_ID);
            conn.setRequestMethod(HTTP_GET);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(conn.getInputStream());
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("item");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node n = nodeList.item(i);
                Node hash = n.getFirstChild();
                hashList.add(hash.getTextContent());
            }
        }
        catch (Exception e) {
            Log.e(LOGTAG, "exception in getImageHashes", e);
        }
        return hashList;
    }

    private static String getImgurUrl(String imageHash, IMAGE_SIZE size) {
        return IMGUR_BASE_URL + imageHash + size.suffix + IMAGE_EXTENSION;
    }

    /** taken from android developers guide **/
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d(LOGTAG, "sample size was " + inSampleSize);
        return inSampleSize;
    }

    /** taken from android developers guide **/
    public static Bitmap decodeSampledBitmapFromStream(InputStream stream, int reqWidth, int reqHeight) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = stream.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
        InputStream is2 = new ByteArrayInputStream(baos.toByteArray());

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is1, new Rect(0, 0, reqWidth, reqHeight), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is2, new Rect(0, 0, reqWidth, reqHeight), options);
    }

    public static Intent shareCat(Bitmap cat) {
        File root = Environment.getExternalStorageDirectory();
        File cachePath = new File(root.getAbsolutePath() + "/DCIM/Camera/image.jpg");
        try {
            cachePath.createNewFile();
            FileOutputStream ostream = new FileOutputStream(cachePath);
            cat.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            ostream.close();
        }
        catch (Exception e) {
            Log.e(LOGTAG, "share cat exception", e);
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cachePath));
        share.putExtra(Intent.EXTRA_TEXT, "Sent using my Katie App!");
        return Intent.createChooser(share, "Share via");
    }

    public static Intent saveCat(Bitmap cat) {
            File directory = getAlbumStorageDir(CAT_DIRECTORY_NAME);
            String filename = getUniqueFilename();
            File newPicture = new File(directory, filename);

            try {
                FileOutputStream out = new FileOutputStream(newPicture);
                cat.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            }
            catch (Exception e) {
                Log.e(LOGTAG, "save cat exception", e);
                return null;
            }

            return notifyFilesystemIntent(newPicture);
    }

    /**
     * This one is called after a cat is undeleted (user presses undo on snackbar)
     */
    public static Intent saveCat(Bitmap cat, Uri oldUri) {
        File newPicture = new File(oldUri.getPath());
        try {
            FileOutputStream out = new FileOutputStream(newPicture);
            cat.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            Log.e(LOGTAG, "save cat exception", e);
            return null;
        }

        return notifyFilesystemIntent(newPicture);
    }

    public static void deleteCat(Context context, Uri cat) {
        File file = new File(cat.getPath());
        boolean deleted = file.exists() && file.delete();

        if (deleted) {
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(cat);
            context.sendBroadcast(scanIntent);
        }
    }

    /* returns intent to make os aware of the new image (i.e. it'll be in gallery) */
    private static Intent notifyFilesystemIntent(File newFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(newFile);
        mediaScanIntent.setData(contentUri);
        return mediaScanIntent;
    }

    private static File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(LOGTAG, "Directory not created (probably already exists)");
        }
        return file;
    }

    /* works by getting a list of filenames, extracting the numerical portion of the names, and
     * using the highest number + 1 for the new file. so if cat_51.png is currently the highest,
     * the new image will be called cat_52.png. if directory has no files we drop through to the
     * return statement which will name the image cat_1.png as it adds 1 to the default value of 0 */
    private static String getUniqueFilename() {
        String prefix = "cat_";
        String suffix = ".png";
        int currentHighest = 0;

        File catDirectory = getAlbumStorageDir(CAT_DIRECTORY_NAME);
        String[] allFiles = catDirectory.list();

        if (allFiles == null) {
            allFiles = new String[]{};
        }

        if (allFiles.length > 0) {
            Log.d(LOGTAG, "current files are: " + mkstr(allFiles));
            for (String f : allFiles) {
                int fileNo = Integer.parseInt(f.substring(4, f.length() - 4));            // extract the number by removing 'cat_' and '.png'
                if (fileNo > currentHighest) {
                    currentHighest = fileNo;
                }
            }
        }

        String filename = prefix + (currentHighest + 1) + suffix;
        Log.d(LOGTAG, "unique filename returned " + filename);
        return filename;
    }

    /* helper method for the log in getUniqueFilename. makes array of filenames into a string to be printed */
    private static String mkstr(String[] arr) {
        String res = "";
        for (String s : arr)
            res += s + ", ";
        return res;
    }
}
