import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

// 物品類
class Item {
  private LocalDateTime productionTime;
  private int id;

  public Item(int id) {
    this.productionTime = LocalDateTime.now();
    this.id = id;
  }

  public LocalDateTime getProductionTime() {
    return productionTime;
  }

  public int getId() {
    return id;
  }

  @Override
  public String toString() {
    return "Item{" +
        "productionTime=" + productionTime +
        ", id=" + id +
        '}';
  }
}

// 緩衝區類
class Buffer {
  private final int capacity;
  private final Queue<Item> queue;

  public Buffer(int capacity) {
    this.capacity = capacity;
    this.queue = new LinkedList<>();
  }

  public synchronized void addItem(Item item) throws InterruptedException {
    while (queue.size() == capacity) {
      wait();
    }
    queue.add(item);
    notifyAll();
  }

  public synchronized Item removeItem() throws InterruptedException {
    while (queue.isEmpty()) {
      wait();
    }
    Item item = queue.poll();
    notifyAll();
    return item;
  }
}

// 生產者執行緒
class ProducerThread extends Thread {
  private final Buffer buffer;
  private final int intervalMillis;

  public ProducerThread(Buffer buffer, int intervalMillis) {
    this.buffer = buffer;
    this.intervalMillis = intervalMillis;
  }

  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        int id = 100 + (int) (Math.random() * 900);
        Item item = new Item(id);
        buffer.addItem(item);
        System.out.println("Produced: " + item);
        Thread.sleep(intervalMillis);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}

// GUI 界面
public class MainFrame extends JFrame {
  private JTextField bufferSizeField;
  private JButton startButton;
  private Buffer buffer;
  private ProducerThread producerThread;

  public MainFrame() {
    setTitle("Producer Thread Example");
    setSize(300, 150);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(null);

    JLabel bufferSizeLabel = new JLabel("Buffer Size:");
    bufferSizeLabel.setBounds(10, 10, 80, 25);
    add(bufferSizeLabel);

    bufferSizeField = new JTextField("10");
    bufferSizeField.setBounds(100, 10, 100, 25);
    add(bufferSizeField);

    startButton = new JButton("Start Production");
    startButton.setBounds(10, 50, 200, 30);
    add(startButton);

    startButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startProduction();
      }
    });
  }

  private void startProduction() {
    try {
      int bufferSize = Integer.parseInt(bufferSizeField.getText());
      if (bufferSize <= 0) {
        JOptionPane.showMessageDialog(this, "Buffer size must be greater than 0.");
        return;
      }
      buffer = new Buffer(bufferSize);
      producerThread = new ProducerThread(buffer, 200); // 0.2秒
      producerThread.start();
      startButton.setEnabled(false);
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this, "Invalid buffer size.");
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      MainFrame frame = new MainFrame();
      frame.setVisible(true);
    });
  }
}
