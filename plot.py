import json
from os import listdir
from pandas import DataFrame, read_csv
from matplotlib import pyplot as plt
from util import read_config_file

def get_datas() -> dict[int, list[str]]:
  res = {}
  file = "config.json"
  with open(file, "r") as f:
    # Load json
    data = json.load(f)
    # Get datas
    for indicator in data["indicators"]:
      indicator_name = indicator["name"]
      params = indicator.get("params", {})

      if indicator_name == "EMA" or indicator_name == "SMA" or indicator_name == "RSI":
        indicator_name = indicator_name + str(params.get("period", 50))

      plot_id = indicator["plot"]
      if plot_id not in res:
        res[plot_id] = []
      
      res[plot_id].append(indicator_name)
  return res


def plot(df: DataFrame, trades: DataFrame):
   
  time = df["date"]
  close = df["close"]
  ploting = get_datas()
  coolors = ["blue", "red", "salmon", "orange", "brown", "green", "cyan", "purple", "pink", "magenta", "gray"]
  def pick_color() -> str:
    return coolors.pop(0)
  
  plot_count = len(ploting)
  # register wallet plot
  plot_count += 1
  # generate height ratio for plot count
  # eg : 2 plots => height_ratios = [3, 1]
  # eg : 3 plots => height_ratios = [3, 1, 1]
  # etc...
  height_ratios = [3] + [1] * (plot_count - 1) 


  # Plot
  f, plots =  plt.subplots(plot_count, 1, gridspec_kw={'height_ratios': height_ratios}, sharex=True)

  plots[0].plot(time, close, linestyle="-", color=pick_color(), label="Prix de clôture")

  for plot_id, indicators in ploting.items():
    axis : plt.Axes = plots[plot_id]
    for indicator in indicators:
      if indicator in df:
        axis.plot(time, df[indicator], linestyle="-", color=pick_color(), label=indicator)
  
  # Last plot = wallet
  wallet_ax : plt.Axes = plots[-1]
  wallet_ax.plot(trades["date"], trades["value"], linestyle="-", color=pick_color(), label="Wallet")
  wallet_ax.set_title("Wallet")
  wallet_ax.set_xlabel("Date")
  wallet_ax.set_ylabel("Valeur")
  wallet_ax.legend()

  plots[1].set_xlabel("Date")
  plots[1].set_ylabel("Bruit")
  plots[1].set_title("Tendance - Marché = Bruit")
  plots[1].legend()

  plots[1].axhline(y = 500, color = 'b', linestyle = '-')
  plots[1].axhline(y = -500, color = 'b', linestyle = '-')
  # set the max y value to be 1000
  plots[1].set_ylim([-1000, 1000])

  # plt.axhline(y = 0.5, color = 'r', linestyle = '-')

  plots[0].set_xlabel("Date")
  plots[0].set_ylabel("Prix")
  plots[0].set_title("Prix de clôture")
  plots[0].legend()

  f.show()
  plt.show()




if __name__ == "__main__":
  print("Plotting...")
  config_file = read_config_file()
  if config_file is None:
    print("No config file found")
    exit(1)

  pairs = config_file["pairs"]
  # Ask user for pair
  print("Pairs:")
  for i in range(len(pairs)):
    print(f"{(i+1)}: {pairs[i]}")
  
  # Get pair
  input_str = input("Select pair to plot:")
  selected_index = int(input_str)
  if selected_index > len(pairs) or selected_index < 1:
    print("Quitting ...")
    exit(1)

  selected_pair: str = pairs[selected_index - 1]
  pair_with_indicators = f"{selected_pair.replace('_', '')}_indicators.csv"
  # Open file
  df = read_csv(f"data/{pair_with_indicators}")

  # Load all strategies
  strategies_name: list[str] = [x["name"] for x in config_file.get("strategies", [])]

  # Ask user for strategy
  print("Strategies:")
  for i in range(len(strategies_name)):
    print(f"{(i+1)}: {strategies_name[i]}")
  
  # Get strategy
  input_str = input("Select strategy to plot:")
  selected_index = int(input_str)
  if selected_index > len(strategies_name) or selected_index < 1:
    print("Quitting ...")
    exit(1) 

  selected_strategy: str = strategies_name[selected_index - 1]
  result_file = f"{selected_pair.replace('_', '')}_{selected_strategy}.csv"

  if result_file not in listdir("results"):
    print(f"No result file found ({result_file})")
    exit(1)

  # Open file
  df2 = read_csv(f"results/{result_file}")
    

  # Plot
  plot(df, df2)