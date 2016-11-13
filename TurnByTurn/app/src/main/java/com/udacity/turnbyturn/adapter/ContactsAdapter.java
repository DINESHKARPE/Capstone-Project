package com.udacity.turnbyturn.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.widget.CursorAdapter;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.turnbyturn.R;
import com.udacity.turnbyturn.model.SelectedContact;
import com.udacity.turnbyturn.util.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by TechSutra on 10/13/16.
 */

public class ContactsAdapter extends CursorAdapter implements SectionIndexer {

    private LayoutInflater mInflater;
    private AlphabetIndexer mAlphabetIndexer;
    private TextAppearanceSpan highlightTextSpan;
    private ImageLoader mImageLoader;
    private List<SelectedContact> selectedContacts;
    private Set<Integer> selectedItemsPositions;
    private Map<Integer,JSONObject> selectedContactMap;
    private Set<Integer> alredySendInvitation;

    /**
     * Instantiates a new Contacts Adapter.
     * @param context A context that has access to the app's layout.
     * @param selectedContacts
     */
    public ContactsAdapter(Context context, List<SelectedContact> selectedContacts) {
        super(context, null, 0);
        mInflater = LayoutInflater.from(context);
        final String alphabet = context.getString(R.string.alphabet);
        mAlphabetIndexer = new AlphabetIndexer(null, ContactsQuery.SORT_KEY, alphabet);
        selectedItemsPositions = new HashSet<>();
        selectedContacts = new ArrayList<>();
        selectedContactMap = new HashMap<>();
        alredySendInvitation = new HashSet<>();
    }



    /**
     * Overrides newView() to inflate the list item views.
     */
    @Override
    public View newView(final Context context, final Cursor cursor, ViewGroup viewGroup) {

        final View itemLayout =
                mInflater.inflate(R.layout.fragment_contact_invitation, viewGroup, false);

        final ViewHolder holder = new ViewHolder();

        holder.imageView = (ImageView) itemLayout.findViewById(R.id.contact_profile_img);
        holder.contactName = (TextView) itemLayout.findViewById(R.id.contact_name);
        holder.contactNumber = (TextView) itemLayout.findViewById(R.id.contact_number);
        holder.checkBox = (CheckBox) itemLayout.findViewById(R.id.select_checkbox);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int position = (int) compoundButton.getTag();

                    if(holder.checkBox.isChecked()){
                        if (!selectedItemsPositions.contains(position)) {
                            getSelectedItemsPositions().add(position);
                            cursor.moveToPosition(position);
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("contactName",cursor.getString(1));
                                jsonObject.put("contactNumber",cursor.getString(2));
                                selectedContactMap.put(position,jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                    else{
                        getSelectedItemsPositions().remove(position);
                        selectedContactMap.remove(position);
                        holder.checkBox.setChecked(false);
                    }


            }
        });

        itemLayout.setTag(holder);

        return itemLayout;
    }

    /**
     * Binds data from the Cursor to the provided view.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final ViewHolder contactItemholder = (ViewHolder) view.getTag();

        final String photoUri = cursor.getString(3);

        final String displayName = cursor.getString(1);
        final String contactNumber = cursor.getString(2);

        contactItemholder.contactName.setText(displayName);
        contactItemholder.contactNumber.setText(contactNumber);

        CheckBox box = (CheckBox) view.findViewById(R.id.select_checkbox);
        box.setTag(cursor.getPosition());




        if (getSelectedItemsPositions().contains(cursor.getPosition())){
            box.setChecked(true);

        }else{
            box.setChecked(false);
        }



        mImageLoader.loadImage(photoUri, contactItemholder.imageView);
    }

    /**
     * Overrides swapCursor to move the new Cursor into the AlphabetIndex as well as the
     * CursorAdapter.
     */
    @Override
    public Cursor swapCursor(Cursor newCursor) {
        // Update the AlphabetIndexer with new cursor as well
        mAlphabetIndexer.setCursor(newCursor);
        return super.swapCursor(newCursor);
    }

    /**
     * An override of getCount that simplifies accessing the Cursor. If the Cursor is null,
     * getCount returns zero. As a result, no test for Cursor == null is needed.
     */
    @Override
    public int getCount() {
        if (getCursor() == null) {
            return 0;
        }
        return super.getCount();
    }

    /**
     * Defines the SectionIndexer.getSections() interface.
     */
    @Override
    public Object[] getSections() {
        return mAlphabetIndexer.getSections();
    }

    /**
     * Defines the SectionIndexer.getPositionForSection() interface.
     */
    @Override
    public int getPositionForSection(int i) {
        if (getCursor() == null) {
            return 0;
        }
        return mAlphabetIndexer.getPositionForSection(i);
    }

    /**
     * Defines the SectionIndexer.getSectionForPosition() interface.
     */
    @Override
    public int getSectionForPosition(int i) {
        if (getCursor() == null) {
            return 0;
        }
        return mAlphabetIndexer.getSectionForPosition(i);
    }



    /**
     * A class that defines fields for each resource ID in the list item layout. This allows
     * ContactsAdapter.newView() to store the IDs once, when it inflates the layout, instead of
     * calling findViewById in each iteration of bindView.
     */
    private class ViewHolder {
        TextView contactName;
        TextView contactNumber;
        ImageView imageView;
        CheckBox checkBox;
    }

    public interface ContactsQuery {

        final static int QUERY_ID = 1;
        final static Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        final static Uri FILTER_URI = ContactsContract.Contacts.CONTENT_FILTER_URI;
        final static String SORT_ORDER =  ContactsContract.Contacts.DISPLAY_NAME;


        final static String[] PROJECTION = {

                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Contacts.PHOTO_URI,
                SORT_ORDER,

        };

        final static int ID = 0;
        final static int LOOKUP_KEY = 1;
        final static int DISPLAY_NAME = 2;
        final static int NUMBER = 3;
        final static int PHOTO_URI=4 ;
        final static int SORT_KEY = 5;
    }

    public Set<Integer> getSelectedItemsPositions() {
        return selectedItemsPositions;
    }

    public void setSelectedItemsPositions(Set<Integer> selectedItemsPositions) {
        this.selectedItemsPositions = selectedItemsPositions;
    }

    public ImageLoader getmImageLoader() {
        return mImageLoader;
    }

    public void setmImageLoader(ImageLoader mImageLoader) {
        this.mImageLoader = mImageLoader;
    }

    public Map<Integer, JSONObject> getSelectedContactMap() {
        return selectedContactMap;
    }

    public void setSelectedContactMap(Map<Integer, JSONObject> selectedContactMap) {
        this.selectedContactMap = selectedContactMap;
    }
    public void updateContactAdapter(){
        notifyDataSetChanged();
    }
}