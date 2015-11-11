package com.nightonke.saver;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jungly.gridpasswordview.Util;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SelectedValue;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.PieChartView;

/**
 * Created by 伟平 on 2015/10/20.
 */

public class TagViewRecyclerViewAdapter
        extends RecyclerView.Adapter<TagViewRecyclerViewAdapter.viewHolder> {

    private Context mContext;

    private List<List<Record>> contents;
    private List<Integer> type;
    private List<Double> SumList;
    private List<Map<Integer, Double>> AllTagExpanse;
    private int[] DayExpanseSum;
    private int[] MonthExpanseSum;
    private int[] SelectedPosition;
    private float Sum;
    private int year;
    private int month;
    private int startYear;
    private int startMonth;
    private int endYear;
    private int endMonth;

    private String dialogTitle;

    private int chartType;

    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;

    static final Integer SHOW_IN_YEAR = 0;
    static final Integer SHOW_IN_MONTH = 1;

    static final int PIE = 0;
    static final int SUM_HISTOGRAM = 1;
    static final int HISTOGRAM = 2;

    private int fragmentPosition;
    private int fragmentTagId;

    public TagViewRecyclerViewAdapter(List<Record> records, Context context, int position) {
        mContext = context;
        fragmentPosition = position;
        if (position == 0) {
            chartType = PIE;
        } else if (position == 1) {
            chartType = SUM_HISTOGRAM;
        } else {
            chartType = HISTOGRAM;
        }

        Sum = 0;
        Collections.sort(records, new Comparator<Record>() {
            @Override
            public int compare(Record lhs, Record rhs) {
                return rhs.getCalendar().compareTo(lhs.getCalendar());
            }
        });
        contents = new ArrayList<>();
        SumList = new ArrayList<>();
        type = new ArrayList<>();
        year = records.get(0).getCalendar().get(Calendar.YEAR);
        month = records.get(0).getCalendar().get(Calendar.MONTH) + 1;
        endYear = year;
        endMonth = month;
        int yearPosition = 0;
        double monthSum = 0;
        double yearSum = 0;
        List<Record> yearSet = new ArrayList<>();
        List<Record> monthSet = new ArrayList<>();
        for (Record record : records) {
            Sum += record.getMoney();
            if (record.getCalendar().get(Calendar.YEAR) == year) {
                yearSet.add(record);
                yearSum += record.getMoney();
                if (record.getCalendar().get(Calendar.MONTH) == month - 1) {
                    monthSet.add(record);
                    monthSum += record.getMoney();
                } else {
                    contents.add(monthSet);
                    SumList.add(monthSum);
                    monthSum = record.getMoney();
                    type.add(SHOW_IN_MONTH);
                    monthSet = new ArrayList<>();
                    monthSet.add(record);
                    month = record.getCalendar().get(Calendar.MONTH) + 1;
                }
            } else {
                contents.add(monthSet);
                SumList.add(monthSum);
                monthSum = record.getMoney();
                type.add(SHOW_IN_MONTH);
                monthSet = new ArrayList<>();
                monthSet.add(record);
                month = record.getCalendar().get(Calendar.MONTH) + 1;

                contents.add(yearPosition, yearSet);
                SumList.add(yearPosition, yearSum);
                yearSum = record.getMoney();
                type.add(yearPosition, SHOW_IN_YEAR);
                yearPosition = contents.size();
                yearSet = new ArrayList<>();
                yearSet.add(record);
                year = record.getCalendar().get(Calendar.YEAR);
                monthSet = new ArrayList<>();
                monthSet.add(record);
                month = record.getCalendar().get(Calendar.MONTH) + 1;
            }
        }
        contents.add(monthSet);
        SumList.add(monthSum);
        type.add(SHOW_IN_MONTH);
        contents.add(yearPosition, yearSet);
        SumList.add(yearPosition, yearSum);
        type.add(yearPosition, SHOW_IN_YEAR);

        startYear = year;
        startMonth = month;

        if (chartType == PIE) {
            AllTagExpanse = new ArrayList<>();
            for (int i = 0; i < contents.size(); i++) {

                Map<Integer, Double> tagExpanse = new TreeMap<>();

                for (Tag tag : RecordManager.TAGS) {
                    tagExpanse.put(tag.getId(), Double.valueOf(0));
                }

                for (Record record : contents.get(i)) {
                    double d = tagExpanse.get(record.getTag());
                    d += record.getMoney();
                    tagExpanse.put(record.getTag(), d);
                }

                tagExpanse = Utils.SortTreeMapByValues(tagExpanse);

                AllTagExpanse.add(tagExpanse);
            }
        }

        if (chartType == SUM_HISTOGRAM) {
            DayExpanseSum = new int[(endYear - startYear + 1) * 372];
            for (Record record : records) {
                DayExpanseSum[(record.getCalendar().get(Calendar.YEAR) - startYear) * 372 +
                        record.getCalendar().get(Calendar.MONTH) * 31 +
                        record.getCalendar().get(Calendar.DAY_OF_MONTH) - 1] += (int)record.getMoney();
            }
        }

        MonthExpanseSum = new int[(endYear - startYear + 1) * 12];
        for (Record record : records) {
            MonthExpanseSum[(record.getCalendar().get(Calendar.YEAR) - startYear) * 12 +
                    record.getCalendar().get(Calendar.MONTH)] += (int)record.getMoney();
        }

        SelectedPosition = new int[contents.size() + 1];
        for (int i = 0; i < SelectedPosition.length; i++) {
            SelectedPosition[i] = 0;
        }

        fragmentTagId = contents.get(0).get(0).getTag();
        if (fragmentPosition == 0) {
            fragmentTagId = -2;
        }
        if (fragmentPosition == 1) {
            fragmentTagId = -1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return TYPE_HEADER;
            default:
                return TYPE_CELL;
        }
    }

    @Override
    public int getItemCount() {
        return contents.size() + 1;
    }

    @Override
    public TagViewRecyclerViewAdapter.viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;

        switch (viewType) {
            case TYPE_HEADER: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.tag_list_view_head, parent, false);
                return new viewHolder(view) {
                };
            }
            case TYPE_CELL: {
                switch (chartType) {
                    case PIE:
                        view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.tag_list_view_pie_body, parent, false);
                        return new viewHolder(view) {
                        };
                    case HISTOGRAM:
                        view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.tag_list_view_histogram_body, parent, false);
                        return new viewHolder(view) {
                        };
                    case SUM_HISTOGRAM:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.tag_list_view_histogram_body, parent, false);
                        return new viewHolder(view) {
                        };
                }
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final viewHolder holder, final int position) {

        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                holder.from.setText("From " + startYear + " " + Utils.MONTHS_SHORT[startMonth]);
                holder.sum.setText((int)Sum + "");
                holder.to.setText("To " + endYear + " " + Utils.MONTHS_SHORT[endMonth]);
                holder.from.setTypeface(Utils.typefaceLatoLight);
                holder.sum.setTypeface(Utils.typefaceLatoLight);
                holder.to.setTypeface(Utils.typefaceLatoLight);
                break;
            case TYPE_CELL:
                int year = contents.get(position - 1).get(0).getCalendar().get(Calendar.YEAR);
                int month = contents.get(position - 1).get(0).getCalendar().get(Calendar.MONTH) + 1;
                ColumnChartData columnChartData;
                PieChartData pieChartData;
                List<SubcolumnValue> subcolumnValues;
                final List<Column> columns;
                final List<SliceValue> sliceValues;
                holder.date.setTypeface(Utils.typefaceLatoLight);
                holder.expanse.setTypeface(Utils.typefaceLatoLight);
                switch (chartType) {
                    case PIE:
                        sliceValues = new ArrayList<>();
                        for (Map.Entry<Integer, Double> entry :
                                AllTagExpanse.get(position - 1).entrySet()) {
                            if (entry.getValue() >= 1) {
                                SliceValue sliceValue = new SliceValue(
                                        (float)(double)entry.getValue(),
                                        mContext.getResources().
                                                getColor(Utils.GetTagColor(entry.getKey())));
                                sliceValue.setLabel(String.valueOf(entry.getKey()));
                                sliceValues.add(sliceValue);
                            }
                        }

                        pieChartData = new PieChartData(sliceValues);
                        pieChartData.setHasLabels(false);
                        pieChartData.setHasLabelsOnlyForSelected(false);
                        pieChartData.setHasLabelsOutside(false);
                        pieChartData.setHasCenterCircle(false);

                        holder.pie.setPieChartData(pieChartData);
                        holder.pie.setOnValueTouchListener(new PieValueTouchListener(position - 1));
                        holder.pie.setChartRotationEnabled(false);

                        if (type.get(position - 1).equals(SHOW_IN_MONTH)) {
                            holder.date.setText(year + " " + Utils.MONTHS_SHORT[month]);
                        } else {
                            holder.date.setText(year + " ");
                        }

                        holder.expanse.setText("" + (int) (double) SumList.get(position - 1));

                        holder.iconRight.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SelectedPosition[position]
                                        = (SelectedPosition[position] + 1) % sliceValues.size();
                                SelectedValue selectedValue =
                                        new SelectedValue(
                                                SelectedPosition[position],
                                                0,
                                                SelectedValue.SelectedValueType.NONE);
                                holder.pie.selectValue(selectedValue);
                            }
                        });

                        holder.iconLeft.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SelectedPosition[position]
                                        = (SelectedPosition[position] - 1 + sliceValues.size()) % sliceValues.size();
                                SelectedValue selectedValue =
                                        new SelectedValue(
                                                SelectedPosition[position],
                                                0,
                                                SelectedValue.SelectedValueType.NONE);
                                holder.pie.selectValue(selectedValue);
                            }
                        });

                        break;
                    case SUM_HISTOGRAM:
                        columns = new ArrayList<>();
                        if (type.get(position - 1).equals(SHOW_IN_YEAR)) {
                            int numColumns = 12;
                            for (int i = 0; i < numColumns; i++) {
                                subcolumnValues = new ArrayList<>();
                                SubcolumnValue value = new SubcolumnValue(
                                        MonthExpanseSum[(year - startYear) * 12 + i],
                                        Utils.GetRandomColor());
                                value.setLabel(Utils.MONTHS_SHORT[month] + " " + year);
                                subcolumnValues.add(value);
                                Column column = new Column(subcolumnValues);
                                column.setHasLabels(false);
                                column.setHasLabelsOnlyForSelected(false);
                                columns.add(column);
                            }

                            columnChartData = new ColumnChartData(columns);

                            Axis axisX = new Axis();
                            List<AxisValue> axisValueList = new ArrayList<>();
                            for (int i = 0; i < numColumns; i++) {
                                axisValueList.add(new AxisValue(i)
                                        .setLabel(Utils.GetMonthShort(i + 1)));
                            }
                            axisX.setValues(axisValueList);
                            Axis axisY = new Axis().setHasLines(true);
                            columnChartData.setAxisXBottom(axisX);
                            columnChartData.setAxisYLeft(axisY);
                            columnChartData.setStacked(true);

                            holder.chart.setColumnChartData(columnChartData);
                            holder.chart.setZoomEnabled(false);
                            holder.chart.setOnValueTouchListener(
                                    new ValueTouchListener(position - 1));

                            holder.date.setText(year + "");
                            holder.expanse.setText("" + (int)(double)SumList.get(position - 1));
                        }
                        if (type.get(position - 1).equals(SHOW_IN_MONTH)) {
                            Calendar tempCal = new GregorianCalendar(year, month - 1, 1);
                            int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

                            int numColumns = daysInMonth;

                            for (int i = 0; i < numColumns; ++i) {
                                subcolumnValues = new ArrayList<>();
                                SubcolumnValue value = new SubcolumnValue((float)
                                        DayExpanseSum[(year - startYear) * 372
                                                + (month - 1) * 31 + i],
                                        Utils.GetRandomColor());
                                value.setLabel(Utils.MONTHS_SHORT[month] + " " + (i + 1) + " " + year);
                                subcolumnValues.add(value);
                                Column column = new Column(subcolumnValues);
                                column.setHasLabels(false);
                                column.setHasLabelsOnlyForSelected(false);
                                columns.add(column);
                            }

                            columnChartData = new ColumnChartData(columns);

                            Axis axisX = new Axis();
                            List<AxisValue> axisValueList = new ArrayList<>();
                            for (int i = 0; i < daysInMonth; i++) {
                                axisValueList.add(new AxisValue(i).setLabel(i + 1 + ""));
                            }
                            axisX.setValues(axisValueList);
                            Axis axisY = new Axis().setHasLines(true);
                            columnChartData.setAxisXBottom(axisX);
                            columnChartData.setAxisYLeft(axisY);
                            columnChartData.setStacked(true);

                            holder.chart.setColumnChartData(columnChartData);
                            holder.chart.setZoomEnabled(false);
                            holder.chart.setOnValueTouchListener(new ValueTouchListener(position - 1));

                            holder.date.setText(year + " " + Utils.MONTHS_SHORT[month]);
                            holder.expanse.setText("" + (int)(double)SumList.get(position - 1));
                        }

                        holder.iconRight.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SelectedPosition[position]
                                        = (SelectedPosition[position] + 1) % columns.size();
                                SelectedValue selectedValue =
                                        new SelectedValue(
                                                SelectedPosition[position],
                                                0,
                                                SelectedValue.SelectedValueType.COLUMN);
                                holder.chart.selectValue(selectedValue);
                            }
                        });

                        holder.iconLeft.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SelectedPosition[position]
                                        = (SelectedPosition[position] - 1 + columns.size()) % columns.size();
                                SelectedValue selectedValue =
                                        new SelectedValue(
                                                SelectedPosition[position],
                                                0,
                                                SelectedValue.SelectedValueType.COLUMN);
                                holder.chart.selectValue(selectedValue);
                            }
                        });

                        break;
                    case HISTOGRAM:
                        columns = new ArrayList<>();
                        if (type.get(position - 1).equals(SHOW_IN_YEAR)) {
                            int numColumns = 12;
                            for (int i = 0; i < numColumns; i++) {
                                subcolumnValues = new ArrayList<>();
                                SubcolumnValue value = new SubcolumnValue(
                                        MonthExpanseSum[(year - startYear) * 12 + i],
                                        Utils.GetRandomColor());
                                value.setLabel(Utils.MONTHS_SHORT[month] + " " + year);
                                subcolumnValues.add(value);
                                Column column = new Column(subcolumnValues);
                                column.setHasLabels(false);
                                column.setHasLabelsOnlyForSelected(false);
                                columns.add(column);
                            }

                            columnChartData = new ColumnChartData(columns);

                            Axis axisX = new Axis();
                            List<AxisValue> axisValueList = new ArrayList<>();
                            for (int i = 0; i < numColumns; i++) {
                                axisValueList.add(new AxisValue(i)
                                        .setLabel(Utils.GetMonthShort(i + 1)));
                            }
                            axisX.setValues(axisValueList);
                            Axis axisY = new Axis().setHasLines(true);
                            columnChartData.setAxisXBottom(axisX);
                            columnChartData.setAxisYLeft(axisY);
                            columnChartData.setStacked(true);

                            holder.chart.setColumnChartData(columnChartData);
                            holder.chart.setZoomEnabled(false);
                            holder.chart.setOnValueTouchListener(
                                    new ValueTouchListener(position - 1));

                            holder.date.setText(year + "");
                            holder.expanse.setText("" + (int)(double)SumList.get(position - 1));

                        }
                        if (type.get(position - 1).equals(SHOW_IN_MONTH)) {
                            Calendar tempCal = new GregorianCalendar(year, month - 1, 1);
                            int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

                            int p = contents.get(position - 1).size() - 1;
                            int numColumns = daysInMonth;

                            for (int i = 0; i < numColumns; ++i) {
                                subcolumnValues = new ArrayList<>();
                                while (p >= 0
                                        && contents.get(position - 1).get(p).getCalendar().
                                        get(Calendar.DAY_OF_MONTH) == i + 1) {
                                    SubcolumnValue value = new SubcolumnValue((float)
                                            contents.get(position - 1).get(p).getMoney(),
                                            Utils.GetRandomColor());
                                    value.setLabel(p + "");
                                    subcolumnValues.add(value);
                                    p--;
                                }
                                Column column = new Column(subcolumnValues);
                                column.setHasLabels(false);
                                column.setHasLabelsOnlyForSelected(false);
                                columns.add(column);
                            }

                            columnChartData = new ColumnChartData(columns);

                            Axis axisX = new Axis();
                            List<AxisValue> axisValueList = new ArrayList<>();
                            for (int i = 0; i < daysInMonth; i++) {
                                axisValueList.add(new AxisValue(i).setLabel(i + 1 + ""));
                            }
                            axisX.setValues(axisValueList);
                            Axis axisY = new Axis().setHasLines(true);
                            columnChartData.setAxisXBottom(axisX);
                            columnChartData.setAxisYLeft(axisY);
                            columnChartData.setStacked(true);

                            holder.chart.setColumnChartData(columnChartData);
                            holder.chart.setZoomEnabled(false);
                            holder.chart.setOnValueTouchListener(new ValueTouchListener(position - 1));

                            holder.date.setText(year + " " + Utils.MONTHS_SHORT[month]);
                            holder.expanse.setText("" + (int)(double)SumList.get(position - 1));
                        }

                        holder.iconRight.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SelectedPosition[position]
                                        = (SelectedPosition[position] + 1) % columns.size();
                                SelectedValue selectedValue =
                                        new SelectedValue(
                                                SelectedPosition[position],
                                                0,
                                                SelectedValue.SelectedValueType.NONE);
                                holder.chart.selectValue(selectedValue);
                            }
                        });

                        holder.iconLeft.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SelectedPosition[position]
                                        = (SelectedPosition[position] - 1 + columns.size()) % columns.size();
                                SelectedValue selectedValue =
                                        new SelectedValue(
                                                SelectedPosition[position],
                                                0,
                                                SelectedValue.SelectedValueType.NONE);
                                holder.chart.selectValue(selectedValue);
                            }
                        });

                        break;
                }
        }
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        @Optional
        @InjectView(R.id.from)
        TextView from;
        @Optional
        @InjectView(R.id.sum)
        TextView sum;
        @Optional
        @InjectView(R.id.to)
        TextView to;
        @Optional
        @InjectView(R.id.chart_pie)
        PieChartView pie;
        @Optional
        @InjectView(R.id.chart)
        ColumnChartView chart;
        @Optional
        @InjectView(R.id.date)
        TextView date;
        @Optional
        @InjectView(R.id.expanse)
        TextView expanse;
        @Optional
        @InjectView(R.id.icon_left)
        MaterialIconView iconLeft;
        @Optional
        @InjectView(R.id.icon_right)
        MaterialIconView iconRight;

        viewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    private class ValueTouchListener implements ColumnChartOnValueSelectListener {

        private int position;

        public ValueTouchListener(int position) {
            this.position = position;
        }

        @Override
        public void onValueSelected(final int columnIndex, int subColumnIndex, SubcolumnValue value) {
            Snackbar snackbar =
                    Snackbar.with(mContext)
                            .type(SnackbarType.MULTI_LINE)
                            .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                            .position(Snackbar.SnackbarPosition.BOTTOM)
                            .margin(15, 15)
                            .backgroundDrawable(Utils.GetSnackBarBackground(fragmentTagId))
                            .textColor(Color.WHITE)
                            .textTypeface(Utils.typefaceLatoLight)
                            .actionLabel("Check")
                            .actionLabelTypeface(Utils.typefaceLatoLight)
                            .actionColor(Color.WHITE);
            if (fragmentPosition == SUM_HISTOGRAM) {
                if (type.get(position).equals(SHOW_IN_MONTH)) {
                    String text = "";
                    String timeString = contents.get(position).get(0).getCalendarString();
                    timeString = timeString.substring(6, 9)
                            + " " + (columnIndex + 1) + " "
                            + timeString.substring(timeString.length() - 4, timeString.length());
                    text += "Spend " + (int)value.getValue() + "\n" +
                            "on " + timeString + ".\n";
                    snackbar.text(text);
                    dialogTitle = "Spend " + (int)value.getValue() + " on " + timeString;
                    snackbar.actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            List<Record> shownRecords = new ArrayList<>();
                            boolean isSamed = false;
                            for (Record record : contents.get(position)) {
                                if (record.getCalendar().get(Calendar.DAY_OF_MONTH) == columnIndex + 1) {
                                    shownRecords.add(record);
                                    isSamed = true;
                                } else {
                                    if (isSamed) {
                                        break;
                                    }
                                }
                            }
                            ((FragmentActivity)mContext).getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(new RecordCheckDialog(
                                            mContext, shownRecords, dialogTitle), "MyDialog")
                                    .commit();
                        }
                    });
                    SnackbarManager.show(snackbar);
                }
                if (type.get(position).equals(SHOW_IN_YEAR)) {
                    String text = "";
                    String timeString = Utils.MONTHS_SHORT[columnIndex + 1] + " " +
                            contents.get(position).get(0).getCalendar().get(Calendar.YEAR);
                    text += "Spend " + (int)value.getValue() + "\n" +
                            "in " + timeString + ".\n";
                    snackbar.text(text);
                    dialogTitle = "Spend " + (int)value.getValue() + " in " + timeString;
                    snackbar.actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            List<Record> shownRecords = new ArrayList<>();
                            boolean isSamed = false;
                            for (Record record : contents.get(position)) {
                                if (record.getCalendar().get(Calendar.MONTH) == columnIndex) {
                                    shownRecords.add(record);
                                    isSamed = true;
                                } else {
                                    if (isSamed) {
                                        break;
                                    }
                                }
                            }
                            ((FragmentActivity)mContext).getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(new RecordCheckDialog(
                                            mContext, shownRecords, dialogTitle), "MyDialog")
                                    .commit();
                        }
                    });
                    SnackbarManager.show(snackbar);
                }
            } else {
                if (type.get(position).equals(SHOW_IN_MONTH)) {
                    String text = "";
                    String timeString = contents.get(position).get(0).getCalendarString();
                    timeString = timeString.substring(6, timeString.length());
                    text += "Spend " + (int)value.getValue() +
                            " on " + timeString + "\n" +
                            "in " + contents.get(position).get(0).getTag() + ".\n";
                    snackbar.text(text);
                    dialogTitle = "Spend " + (int)value.getValue() + " on " + timeString + "\n" +
                            "in " + contents.get(position).get(0).getTag();
                    snackbar.actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            List<Record> shownRecords = new ArrayList<>();
                            boolean isSamed = false;
                            for (Record record : contents.get(position)) {
                                if (record.getCalendar().
                                        get(Calendar.DAY_OF_MONTH) == columnIndex + 1) {
                                    shownRecords.add(record);
                                    isSamed = true;
                                } else {
                                    if (isSamed) {
                                        break;
                                    }
                                }
                            }
                            ((FragmentActivity)mContext).getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(new RecordCheckDialog(
                                            mContext, shownRecords, dialogTitle), "MyDialog")
                                    .commit();
                        }
                    });
                    SnackbarManager.show(snackbar);
                }
                if (type.get(position).equals(SHOW_IN_YEAR)) {
                    String text = "";
                    String timeString = Utils.MONTHS_SHORT[columnIndex + 1] + " " +
                            contents.get(position).get(0).getCalendar().get(Calendar.YEAR);
                    text += "Spend " + (int)value.getValue() + "\n" +
                            " in " + timeString + ".\n";
                    snackbar.text(text);
                    dialogTitle = "Spend " + (int)value.getValue() + " in " + timeString + "\n" +
                            "in " + contents.get(position).get(0).getTag();
                    snackbar.actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            List<Record> shownRecords = new ArrayList<>();
                            boolean isSamed = false;
                            for (Record record : contents.get(position)) {
                                if (record.getCalendar().get(Calendar.MONTH) == columnIndex) {
                                    shownRecords.add(record);
                                    isSamed = true;
                                } else {
                                    if (isSamed) {
                                        break;
                                    }
                                }
                            }
                            ((FragmentActivity)mContext).getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(new RecordCheckDialog(
                                            mContext, shownRecords, dialogTitle), "MyDialog")
                                    .commit();
                        }
                    });
                    SnackbarManager.show(snackbar);
                }
            }
        }

        @Override
        public void onValueDeselected() {

        }

    }

    private class PieValueTouchListener implements PieChartOnValueSelectListener {

        private int position;

        public PieValueTouchListener(int position) {
            this.position = position;
        }

        @Override
        public void onValueSelected(int i, SliceValue sliceValue) {
            String text = "";
            String timeString = contents.get(position).get(0).getCalendarString();
            timeString = timeString.substring(6, timeString.length());
            if (type.get(position).equals(SHOW_IN_YEAR)) {
                timeString = " in " + timeString.substring(timeString.length() - 4, timeString.length());
            } else {
                timeString = timeString.substring(0, 3)
                        + " "
                        + timeString.substring(timeString.length() - 4, timeString.length());
                timeString = " on " + timeString;
            }
            final int tagId = Integer.valueOf(String.valueOf(sliceValue.getLabelAsChars()));
            Double percent = sliceValue.getValue() / SumList.get(position) * 100;
            text += "Spend " + (int)sliceValue.getValue()
                    + " (takes " + String.format("%.2f", percent) + "%)\n"
                    + " in " + RecordManager.TAG_NAMES.get(tagId) + ".\n";
            dialogTitle = "Spend " + (int)sliceValue.getValue() + timeString + "\n" +
                    " in " + RecordManager.TAG_NAMES.get(tagId);
            Snackbar snackbar =
                    Snackbar
                            .with(mContext)
                            .type(SnackbarType.MULTI_LINE)
                            .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                            .position(Snackbar.SnackbarPosition.BOTTOM)
                            .margin(15, 15)
                            .backgroundDrawable(Utils.GetSnackBarBackground(fragmentTagId))
                            .text(text)
                            .textTypeface(Utils.typefaceLatoLight)
                            .textColor(Color.WHITE)
                            .actionLabelTypeface(Utils.typefaceLatoLight)
                            .actionLabel("Check")
                            .actionColor(Color.WHITE)
                            .actionListener(new ActionClickListener() {
                                @Override
                                public void onActionClicked(Snackbar snackbar) {
                                    List<Record> shownRecords = new ArrayList<>();
                                    for (Record record : contents.get(position)) {
                                        if (record.getTag() == tagId) {
                                            shownRecords.add(record);
                                        }
                                    }
                                    ((FragmentActivity)mContext).getSupportFragmentManager()
                                            .beginTransaction()
                                            .add(new RecordCheckDialog(
                                                    mContext, shownRecords, dialogTitle), "MyDialog")
                                            .commit();
                                }
                            });
            SnackbarManager.show(snackbar);
        }

        @Override
        public void onValueDeselected() {

        }
    }

}