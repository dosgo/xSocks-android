package io.github.xSocks.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.materialdrawer.R;
import com.mikepenz.materialdrawer.model.BaseDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.ColorfulBadgeable;
import com.mikepenz.materialdrawer.util.PressedEffectStateListDrawable;
import com.mikepenz.materialdrawer.util.UIUtils;

public class ProfileDrawerItem extends BaseDrawerItem<ProfileDrawerItem> implements ColorfulBadgeable<ProfileDrawerItem> {
    private String description;
    private int descriptionRes = -1;

    private String badge;
    private int badgeTextColor = 0;
    private int id;

    public ProfileDrawerItem withProfileId(int id) {
        this.id = id;
        return this;
    }

    public int getProfileId() {
        return id;
    }

    public ProfileDrawerItem withDescription(String description) {
        this.description = description;
        return this;
    }

    public ProfileDrawerItem withDescription(int descriptionRes) {
        this.descriptionRes = descriptionRes;
        return this;
    }

    @Override
    public ProfileDrawerItem withBadge(String badge) {
        this.badge = badge;
        return this;
    }

    @Override
    public ProfileDrawerItem withBadgeTextColor(int color) {
        this.badgeTextColor = color;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDescriptionRes() {
        return descriptionRes;
    }

    public void setDescriptionRes(int descriptionRes) {
        this.descriptionRes = descriptionRes;
    }

    public String getBadge() {
        return badge;
    }

    @Override
    public int getBadgeTextColor() {
        return badgeTextColor;
    }

    @Override
    public void setBadgeTextColor(int color) {
        this.badgeTextColor = color;
    }

    @Override
    public void setBadge(String badge) {
        this.badge = badge;
    }


    @Override
    public String getType() {
        return "PROFILE_ITEM";
    }

    @Override
    public int getLayoutRes() {
        return R.layout.material_drawer_item_primary;
    }

    @Override
    public View convertView(LayoutInflater inflater, View convertView, ViewGroup parent) {
        Context ctx = parent.getContext();

        //get the viewHolder
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(getLayoutRes(), parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //get the correct color for the background
        int selectedColor = UIUtils.decideColor(ctx, getSelectedColor(), getSelectedColorRes(), R.attr.material_drawer_selected, R.color.material_drawer_selected);
        //get the correct color for the text
        int color;
        if (this.isEnabled()) {
            color = UIUtils.decideColor(ctx, getTextColor(), getTextColorRes(), R.attr.material_drawer_primary_text, R.color.material_drawer_primary_text);
        } else {
            color = UIUtils.decideColor(ctx, getDisabledTextColor(), getDisabledTextColorRes(), R.attr.material_drawer_hint_text, R.color.material_drawer_hint_text);
        }
        int selectedTextColor = UIUtils.decideColor(ctx, getSelectedTextColor(), getSelectedTextColorRes(), R.attr.material_drawer_selected_text, R.color.material_drawer_selected_text);
        //get the correct color for the icon
        int iconColor;
        if (this.isEnabled()) {
            iconColor = UIUtils.decideColor(ctx, getIconColor(), getIconColorRes(), R.attr.material_drawer_primary_icon, R.color.material_drawer_primary_icon);
        } else {
            iconColor = UIUtils.decideColor(ctx, getDisabledIconColor(), getDisabledIconColorRes(), R.attr.material_drawer_hint_text, R.color.material_drawer_hint_text);
        }
        int selectedIconColor = UIUtils.decideColor(ctx, getSelectedIconColor(), getSelectedIconColorRes(), R.attr.material_drawer_selected_text, R.color.material_drawer_selected_text);

        //set the background for the item
        UIUtils.setBackground(viewHolder.view, UIUtils.getDrawerItemBackground(selectedColor));

        //set the text for the name
        if (this.getNameRes() != -1) {
            viewHolder.name.setText(this.getNameRes());
        } else {
            viewHolder.name.setText(this.getName());
        }

        //set the text for the description or hide
        viewHolder.description.setVisibility(View.VISIBLE);
        if (this.getDescriptionRes() != -1) {
            viewHolder.description.setText(this.getDescriptionRes());
        } else if (this.getDescription() != null) {
            viewHolder.description.setText(this.getDescription());
        } else {
            viewHolder.description.setVisibility(View.GONE);
        }

        //set the text for the badge or hide
        if (getBadge() != null) {
            viewHolder.badge.setText(getBadge());
            viewHolder.badge.setVisibility(View.VISIBLE);
        } else {
            viewHolder.badge.setVisibility(View.GONE);
        }

        //set the colors for textViews
        viewHolder.name.setTextColor(UIUtils.getTextColorStateList(color, selectedTextColor));
        viewHolder.description.setTextColor(UIUtils.getTextColorStateList(color, selectedTextColor));
        if (badgeTextColor != 0) {
            viewHolder.badge.setTextColor(badgeTextColor);
        } else {
            viewHolder.badge.setTextColor(UIUtils.getTextColorStateList(color, selectedTextColor));
        }

        //define the typeface for our textViews
        if (getTypeface() != null) {
            viewHolder.name.setTypeface(getTypeface());
            viewHolder.description.setTypeface(getTypeface());
            viewHolder.badge.setTypeface(getTypeface());
        }

        //get the drawables for our icon
        Drawable icon = UIUtils.decideIcon(ctx, getIcon(), getIIcon(), getIconRes(), iconColor, isIconTinted());
        Drawable selectedIcon = UIUtils.decideIcon(ctx, getSelectedIcon(), getIIcon(), getSelectedIconRes(), selectedIconColor, isIconTinted());

        //if we have an icon then we want to set it
        if (icon != null) {
            //if we got a different color for the selectedIcon we need a StateList
            if (selectedIcon != null) {
                viewHolder.icon.setImageDrawable(UIUtils.getIconStateList(icon, selectedIcon));
            } else if (isIconTinted()) {
                viewHolder.icon.setImageDrawable(new PressedEffectStateListDrawable(icon, iconColor, selectedIconColor));
            } else {
                viewHolder.icon.setImageDrawable(icon);
            }
            //make sure we display the icon
            viewHolder.icon.setVisibility(View.VISIBLE);
        } else {
            //hide the icon
            viewHolder.icon.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ViewHolder {
        private View view;
        private ImageView icon;
        private TextView name;
        private TextView description;
        private TextView badge;

        private ViewHolder(View view) {
            this.view = view;
            this.icon = (ImageView) view.findViewById(R.id.icon);
            this.name = (TextView) view.findViewById(R.id.name);
            this.description = (TextView) view.findViewById(R.id.description);
            this.badge = (TextView) view.findViewById(R.id.badge);
        }
    }
}

