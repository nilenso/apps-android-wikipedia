package org.wikipedia.readinglist;

import android.database.Cursor;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.Validate;
import org.wikipedia.database.contract.ReadingListContract;
import org.wikipedia.readinglist.database.ReadingListRow;
import org.wikipedia.readinglist.page.ReadingListPage;

import java.util.ArrayList;
import java.util.List;

public final class ReadingList extends ReadingListRow {
    @NonNull private final List<ReadingListPage> pages;

    public static ReadingList fromCursor(@NonNull Cursor cursor) {
        ReadingListRow list = ReadingList.DATABASE_TABLE.fromCursor(cursor);
        List<ReadingListPage> pages = new ArrayList<>();

        cursor.moveToPrevious();
        while (cursor.moveToNext()) {
            ReadingListRow curList = ReadingList.DATABASE_TABLE.fromCursor(cursor);
            if (!curList.key().equals(list.key())) {
                cursor.moveToPrevious();
                break;
            }

            boolean hasRow = ReadingListContract.ListWithPagesAndDisk.PAGE_KEY.val(cursor) != null;
            if (hasRow) {
                ReadingListPage page = ReadingListPage.fromCursor(cursor);
                pages.add(page);
            }
        }

        return ReadingList
                .builder()
                .copy(list)
                .pages(pages)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @NonNull public List<ReadingListPage> getPages() {
        return pages;
    }

    public void remove(@NonNull ReadingListPage page) {
        for (ReadingListPage p : pages) {
            if (p.key().equals(page.key())) {
                pages.remove(p);
                return;
            }
        }
    }

    public void add(@NonNull ReadingListPage page) {
        for (ReadingListPage p : pages) {
            if (p.key().equals(page.key())) {
                return;
            }
        }
        pages.add(0, page);
    }

    public void setTitle(@NonNull String title) {
        title(title);
    }

    public void setDescription(@NonNull String description) {
        description(description);
    }

    public void setSaveOffline(boolean saved) {
        for (ReadingListPage page : pages) {
            page.savedOrSaving(saved);
        }
    }

    public boolean getSaveOffline() {
        for (ReadingListPage page : pages) {
            if (!page.savedOrSaving()) {
                return false;
            }
        }
        return true;
    }

    private ReadingList(@NonNull Builder builder) {
        super(builder);
        pages = new ArrayList<>(builder.pages);
    }

    public static class Builder extends ReadingListRow.Builder<Builder> {
        private List<ReadingListPage> pages;

        public Builder pages(@NonNull List<ReadingListPage> pages) {
            this.pages = new ArrayList<>(pages);
            return this;
        }

        @Override public ReadingList build() {
            validate();
            return new ReadingList(this);
        }

        @Override protected void validate() {
            super.validate();
            Validate.notNull(pages);
        }
    }
}