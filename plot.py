from os import listdir
from pandas import DataFrame, read_csv
from matplotlib import pyplot as plt

def plot(df: DataFrame):
   
  time = df["date"]
  close = df["close"]
  sma50 = df["SMA50"]
  sma200 = df["SMA200"]
  savgol = df["Savgol"]
  noise = df["noise_savgol"]

  # Plot
  f, (a1, a2) =  plt.subplots(2, 1, gridspec_kw={'height_ratios': [3, 1]}, sharex=True)

  a1.plot(time, close, "c-", label="Prix de clôture")
  a1.plot(time, sma50, "r-", label="SMA50")
  a1.plot(time, sma200, "b-", label="SMA200")
  a1.plot(time, savgol, "y-", label="Savgol")
  
  a2.plot(time, noise, "r-", label="Noise")
  a2.set_xlabel("Date")
  a2.set_ylabel("Bruit")
  a2.set_title("Tendance - Marché = Bruit")
  a2.legend()

  a1.set_xlabel("Date")
  a1.set_ylabel("Prix")
  a1.set_title("Prix de clôture")
  a1.legend()

  f.show()
  plt.show()




if __name__ == "__main__":
  print("Plotting...")

  # List files
  files = listdir("data")
  # Ask user for file to plot
  print("Files:")
  for i in range(len(files)):
    print(f"{(i+1)}: {files[i]}")

  # Get file
  input_str = input("Select file to plot:")
  selected_index = int(input_str)
  if selected_index > len(files) or selected_index < 1:
    print("Quitting ...")
    exit(1)
  file = files[selected_index-1]
  # Open file
  df = read_csv(f"data/{file}")

  # Plot
  plot(df)