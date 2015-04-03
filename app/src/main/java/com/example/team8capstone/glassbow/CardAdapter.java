package com.example.team8capstone.glassbow;

import com.example.team8capstone.glassbow.R;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter class that handles list of cards.
 */
public class CardAdapter extends CardScrollAdapter {

    final List<CardBuilder> mCards;
    final List<CardInfo> mCardInfos;

    Context c;

    public CardAdapter(List<CardBuilder> cards, Context context, List<CardInfo> cardInfos) {
        mCards = cards;
        mCardInfos = cardInfos;
        c = context;
    }

    @Override
    public int getCount() {
        return mCards.size();
    }

    @Override
    public Object getItem(int position) {
        return mCards.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mCards.get(position).getView(convertView, parent);

        if (mCardInfos.get(position).hasXmlLayout){
            switch (mCardInfos.get(position).xmlLayout){
                case R.layout.left_column_layout:
                    ViewGroup rightColumn = (ViewGroup) view.findViewById(R.id.right_column);

                    if (mCardInfos.get(position).hasImage){
                        ImageView imageView = (ImageView) rightColumn.findViewById(R.id.image);
                        imageView.setImageResource(mCardInfos.get(position).imageResource);
                    }

                    ViewGroup leftColumn = (ViewGroup) view.findViewById(R.id.left_column);

                    if (mCardInfos.get(position).hasHeader) {
                        TextView textViewHeader = (TextView) leftColumn.findViewById(R.id.header);
                        textViewHeader.setText(mCardInfos.get(position).header);
                        textViewHeader.setTextSize(mCardInfos.get(position).headerTextSize);
                    }

                    if (mCardInfos.get(position).hasText) {
                        TextView textViewContent = (TextView) leftColumn.findViewById(R.id.content);
                        textViewContent.setText(mCardInfos.get(position).text);
                        textViewContent.setTextSize(mCardInfos.get(position).textSize);
                    }
                    return view;
                default:
                    return view;
            }
        }
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return CardBuilder.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position){
        return mCards.get(position).getItemViewType();
    }

    @Override
    public int getPosition(Object item) {
        for (int i = 0; i < mCards.size(); i++) {
            if (getItem(i).equals(item)) {
                return i;
            }
        }
        return AdapterView.INVALID_POSITION;
    }
}
