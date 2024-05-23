import string
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import csv
import pingouin as pg
from scipy.stats import f_oneway
from scipy.stats import shapiro
from scipy.stats import levene
from scipy.stats import friedmanchisquare
from scipy.stats import ttest_rel
from scipy.stats import wilcoxon

df = pd.read_csv('overall.csv')
desc_stat_df = pd.read_csv('descriptive_stats.csv')
types = df.iloc[:, 2].values

car_means = desc_stat_df.iloc[:, 1].values
car_meds = desc_stat_df.iloc[:, 2].values
car_stds = desc_stat_df.iloc[:, 3].values
car_no = desc_stat_df.iloc[:, 4].values

bus_means = desc_stat_df.iloc[:, 5].values
bus_meds = desc_stat_df.iloc[:, 6].values
bus_stds = desc_stat_df.iloc[:, 7].values
bus_no = desc_stat_df.iloc[:, 8].values

truck_means = desc_stat_df.iloc[:, 9].values
truck_meds = desc_stat_df.iloc[:, 10].values
truck_stds = desc_stat_df.iloc[:, 11].values
truck_no = desc_stat_df.iloc[:, 12].values

car_stats, car_p_value = shapiro(car_no)
bus_stats, bus_p_value = shapiro(bus_no)
truck_stats, truck_p_value = shapiro(truck_no)

data = pd.DataFrame({'Car': car_no, 'Bus': bus_no, 'Truck': truck_no})
print('\n')

print("Shapiro-Wilk test of normality:")
print(f'Cars: {car_p_value}')
print(f'Buses: {bus_p_value}')
print(f'Trucks: {truck_p_value}\n')

# Mauchly's test for sphericity
mauchly_result = pg.sphericity(data)
print("Mauchly's test for sphericity:")
print(mauchly_result)
print('\n')

levene_result = levene(car_no, bus_no, truck_no)
print(levene_result)
print('\n')

# One-way ANOVA if data is normally distributed
if car_p_value  > 0.05 and bus_p_value > 0.05 and truck_p_value > 0.05 and mauchly_result['spher'] and levene_result.pvalue > 0.05:
    print("ANOVA test")
    statistic, p_value = f_oneway(car_no, bus_no, truck_no)
    print(f'F-statistic: {statistic}')
    print(f'P-value: {p_value}')
    if p_value > 0.05:
        print("The ANOVA test indicates that there is no statistically significant difference between mean values of number of cars, buses and trucks across the database.")
# Friedman test if data not normally distributed
else:
    print("Friedman test")
    statistic, p_value = friedmanchisquare(car_no, bus_no, truck_no)
    print(f'Statistic: {statistic}')
    print(f'P-value: {p_value}')
    if p_value > 0.05:
        print("The Friedman test indicates that there is no statistically significant difference in the mean values of the number of cars, buses, and trucks across the database.")
print('\n')

#post-hoc
if p_value < 0.05:
    print("------------------------------------------------------------")
    print("Post-HOC of ANOVA/Friedman")
    if car_p_value > 0.05 and bus_p_value > 0.05:
        t_stasts, t_p_value = ttest_rel(car_no, bus_no)
        print("Paired t-test on car and bus data:")
        print(f'T-statistic: {t_stasts}')
        print("Degrees of freedom: " + string(len(car_no) - 1))
        print(f'P-value: {t_p_value}')
        if(t_p_value > 0.05):
            print("The paired t-test indicates that there is no significant difference in mean values of number of cars and buses across the database.")
        if t_p_value <= 0.05:
            if(np.mean(car_no) > np.mean(bus_no)):
                print("The paired t-test suggests a statistically significant difference in mean values of number of cars and buses across the database, with car group having higher mean value.")
            else:
                print("The paired t-test suggests a statistically significant difference in mean values of number of cars and buses across the database, with bus group having higher mean value.")
    else:
        w_stasts, w_p_value = wilcoxon(car_no, bus_no)
        print("Wilcoxon Signed Rank test on car and bus data:")
        print(f'W-statistic: {w_stasts}')
        print(f'P-value: {w_p_value}')
        if(w_p_value > 0.05):
            print("The Wilcoxon Signed Rank test indicates that there is no significant difference in mean values of number of cars and buses across the database.")
        if w_p_value <= 0.05:
            if np.mean(car_no) > np.mean(bus_no):
                print("The Wilcoxon signed-rank test indicates a statistically significant difference in mean values of the number of cars and buses across the database, with the car group having a higher mean value.")
            else:
                print("The Wilcoxon signed-rank test indicates a statistically significant difference in mean values of the number of cars and buses across the database, with the bus group having a higher mean value.")

    print('\n')

    if car_p_value > 0.05 and truck_p_value > 0.05:
        t_stats, t_p_value = ttest_rel(car_no, truck_no)
        print("Paired t-test on car and truck data:")
        print(f'T-statistic: {t_stats}')
        print("Degrees of freedom: " + str(len(car_no) - 1))
        print(f'P-value: {t_p_value}')
        if(t_p_value > 0.05):
            print("The paired t-test indicates that there is no significant difference in mean values of number of cars and trucks across the database.")
        if t_p_value <= 0.05:
            if(np.mean(car_no) > np.mean(truck_no)):
                print("The paired t-test suggests a statistically significant difference in mean values of number of cars and trucks across the database, with car group having higher mean value.")
            else:
                print("The paired t-test suggests a statistically significant difference in mean values of number of cars and trucks across the database, with truck group having higher mean value.")
    else:
        w_stats, w_p_value = wilcoxon(car_no, truck_no)
        print("Wilcoxon Signed Rank test on car and truck data:")
        print(f'W-statistic: {w_stats}')
        print(f'P-value: {w_p_value}')
        if(w_p_value > 0.05):
            print("The Wilcoxon Signed Rank test indicates that there is no significant difference in mean values of number of cars and trucks across the database.")
        if w_p_value <= 0.05:
            if np.mean(car_no) > np.mean(bus_no):
                print("The Wilcoxon signed-rank test indicates a statistically significant difference in mean values of the number of cars and trucks across the database, with the car group having a higher mean value.")
            else:
                print("The Wilcoxon signed-rank test indicates a statistically significant difference in mean values of the number of cars and trucks across the database, with the bus group having a higher mean value.")
    print('\n')

    if bus_p_value > 0.05 and truck_p_value > 0.05:
        t_stats, t_p_value = ttest_rel(bus_no, truck_no)
        print("Paired t-test on bus and truck data:")
        print(f'T-statistic: {t_stats}')
        print("Degrees of freedom: " + str(len(bus_no) - 1))
        print(f'P-value: {t_p_value}')
        if(t_p_value > 0.05):
            print("The paired t-test indicates that there is no significant difference in mean values of number of buses and trucks across the database.")
        if t_p_value <= 0.05:
            if(np.mean(bus_no) > np.mean(truck_no)):
                print("The paired t-test suggests a statistically significant difference in mean values of number of cars and buses across the database, with bus group having higher mean value.")
            else:
                print("The paired t-test suggests a statistically significant difference in mean values of number of cars and buses across the database, with truck group having higher mean value.")
    else:
        w_stats, w_p_value = wilcoxon(bus_no, truck_no)
        print("Wilcoxon Signed Rank test on bus and truck data:")
        print(f'W-statistic: {w_stats}')
        print(f'P-value: {w_p_value}')
        if(w_p_value > 0.05):
            print("The Wilcoxon Signed Rank test indicates that there is no significant difference in mean values of number of buses and trucks across the database.")
        if w_p_value <= 0.05:
            if np.mean(bus_no) > np.mean(truck_no):
                print("The Wilcoxon signed-rank test indicates a statistically significant difference in mean values of the number of buses and trucks across the database, with the bus group having a higher mean value.")
            else:
                print("The Wilcoxon signed-rank test indicates a statistically significant difference in mean values of the number of buses and trucks across the database, with the trucks group having a higher mean value.")
print('\n')

print("Descriptive statistics:")
print(f'Mean value of cars: {np.mean(car_no)}')
print(f'Median value of cars: {np.median(car_no)}')
print(f'Standard deviation of cars: {np.std(car_no)}')

print(f'Mean value of buses: {np.mean(bus_no)}')
print(f'Median value of buses: {np.median(bus_no)}')
print(f'Standard deviation of buses: {np.std(bus_no)}')

print(f'Mean value of trucks: {np.mean(truck_no)}')
print(f'Median value of trucks: {np.median(truck_no)}')
print(f'Standard deviation of trucks: {np.std(truck_no)}\n')


categories = ['Cars', 'Buses', 'Trucks']
means = [np.mean(car_no), np.mean(bus_no), np.mean(truck_no)]
std_devs = [np.std(car_no), np.std(bus_no), np.std(truck_no)]

plt.bar(categories, means, yerr=std_devs, capsize=5, color=['blue', 'orange', 'green'])
plt.xlabel('Vehicle Types')
plt.ylabel('Mean Values of number of detected vehicles')
plt.title('Mean Values with Standard Deviation Error Bars accross whole database')
plt.savefig("overall_barplot.png", format="png")
#plt.show()
plt.close()

trucks = 0
cars = 0
buses = 0
overall = 0

for type in types:
    overall += 1
    if type == 'car':
        cars += 1
    elif type == 'bus':
        buses += 1
    elif type == 'truck':
        trucks += 1

labels = ['Cars', 'Trucks', 'Buses']
sizes = [cars, trucks, buses]
colors = ['red','blue', 'yellow']
explode = (0.1, 0, 0)
plt.pie(sizes, explode=explode, labels=labels, colors=colors, autopct='%1.1f%%', shadow=True, startangle=140)
plt.axis('equal')
plt.title('Overall distribution of different vehicles in all videos')
plt.legend()
plt.savefig("overall.png", format="png")
plt.close()
car_percentage = (cars / overall) * 100
truck_percentage = (trucks / overall) * 100
bus_percentage = (buses / overall) * 100

labels = ['Cars', 'Trucks', 'Buses']
percentages = [car_percentage, truck_percentage, bus_percentage]
colors = ['red', 'orange', 'blue']

plt.bar(labels, percentages, color=colors)
plt.title('Percentage distribution of different vehicles in all videos')
plt.xlabel('Vehicle Types')
plt.ylabel('Percentage (%)')
plt.savefig("percentage_bar_chart.png", format="png")
plt.show()
plt.close()

# Create a DataFrame
data = {'Vehicle Types': labels, 'Percentage': percentages}
df = pd.DataFrame(data)

# Display the DataFrame as a table
fig, ax = plt.subplots(figsize=(8, 3)) 
ax.axis('tight')
ax.axis('off')
ax.table(cellText=df.values, colLabels=df.columns, cellLoc = 'center', loc='center')

plt.title('Percentage distribution of different vehicles in all videos')
plt.savefig("percentage_table.png", format="png")
plt.show()