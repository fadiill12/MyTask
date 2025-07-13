package com.example.mytask;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FileListAdapter extends BaseAdapter {
    private Context context;
    private List<DashboardGuruActivity.FileItem> fileItems;
    private LayoutInflater inflater;

    public FileListAdapter(Context context, List<DashboardGuruActivity.FileItem> fileItems) {
        this.context = context;
        this.fileItems = fileItems;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return fileItems.size();
    }

    @Override
    public Object getItem(int position) {
        return fileItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_file_list, parent, false);
            holder = new ViewHolder();
            holder.ivFileIcon = convertView.findViewById(R.id.ivFileIcon);
            holder.tvFileName = convertView.findViewById(R.id.tvFileName);
            holder.tvFileSize = convertView.findViewById(R.id.tvFileSize);
            holder.tvUploadDate = convertView.findViewById(R.id.tvUploadDate);
            holder.tvFileType = convertView.findViewById(R.id.tvFileType);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DashboardGuruActivity.FileItem fileItem = fileItems.get(position);

        // Set file icon based on type
        int iconResource = getFileIcon(fileItem.format, fileItem.resourceType);
        holder.ivFileIcon.setImageResource(iconResource);

        // Set file details
        holder.tvFileName.setText(fileItem.fileName);
        holder.tvFileSize.setText(fileItem.getFormattedSize());
        holder.tvUploadDate.setText(fileItem.getFormattedDate());
        holder.tvFileType.setText(fileItem.format.toUpperCase());

        return convertView;
    }

    private int getFileIcon(String format, String resourceType) {
        if ("image".equals(resourceType)) {
            return android.R.drawable.ic_menu_gallery;
        } else if ("pdf".equals(format)) {
            return android.R.drawable.ic_menu_agenda;
        } else if ("doc".equals(format) || "docx".equals(format)) {
            return android.R.drawable.ic_menu_edit;
        } else if ("txt".equals(format)) {
            return android.R.drawable.ic_menu_info_details;
        } else {
            return android.R.drawable.ic_menu_save;
        }
    }

    static class ViewHolder {
        ImageView ivFileIcon;
        TextView tvFileName;
        TextView tvFileSize;
        TextView tvUploadDate;
        TextView tvFileType;
    }
}