package de.danoeh.antennapod.util.vorbiscommentreader;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import de.danoeh.antennapod.AppConfig;
import de.danoeh.antennapod.feed.Chapter;
import de.danoeh.antennapod.feed.VorbisCommentChapter;

public class VorbisCommentChapterReader extends VorbisCommentReader {
	private static final String TAG = "VorbisCommentChapterReader";

	private static final String CHAPTER_KEY = "chapter\\d\\d\\d.*";
	private static final String CHAPTER_ATTRIBUTE_TITLE = "name";
	private static final String CHAPTER_ATTRIBUTE_LINK = "url";

	private List<Chapter> chapters;

	public VorbisCommentChapterReader() {
	}

	@Override
	public void onVorbisCommentFound() {
		System.out.println("Vorbis comment found");
	}

	@Override
	public void onVorbisCommentHeaderFound(VorbisCommentHeader header) {
		chapters = new ArrayList<Chapter>();
		System.out.println(header.toString());
	}

	@Override
	public boolean onContentVectorKey(String content) {
		return content.matches(CHAPTER_KEY);
	}

	@Override
	public void onContentVectorValue(String key, String value)
			throws VorbisCommentReaderException {
		if (AppConfig.DEBUG)
			Log.d(TAG, "Key: " + key + ", value: " + value);
		String attribute = VorbisCommentChapter.getAttributeTypeFromKey(key);
		if (attribute == null) {
			int id = VorbisCommentChapter.getIDFromKey(key);
			if (getChapterById(id) == null) {
				// new chapter
				long start = VorbisCommentChapter.getStartTimeFromValue(value);
				VorbisCommentChapter chapter = new VorbisCommentChapter(id);
				chapter.setStart(start);
				chapters.add(chapter);
			} else {
				throw new VorbisCommentReaderException(
						"Found chapter with duplicate ID (" + key + ", "
								+ value + ")");
			}
		} else if (attribute.equals(CHAPTER_ATTRIBUTE_TITLE)) {
			int id = VorbisCommentChapter.getIDFromKey(key);
			Chapter c = getChapterById(id);
			if (c != null) {
				c.setTitle(value);
			}
		}
	}

	@Override
	public void onNoVorbisCommentFound() {
		System.out.println("No vorbis comment found");
	}

	@Override
	public void onEndOfComment() {
		System.out.println("End of comment");
		for (Chapter c : chapters) {
			System.out.println(c.toString());
		}
	}

	@Override
	public void onError(VorbisCommentReaderException exception) {
		exception.printStackTrace();
	}

	private Chapter getChapterById(long id) {
		for (Chapter c : chapters) {
			if (((VorbisCommentChapter) c).getVorbisCommentId() == id) {
				return c;
			}
		}
		return null;
	}

	public List<Chapter> getChapters() {
		return chapters;
	}

}