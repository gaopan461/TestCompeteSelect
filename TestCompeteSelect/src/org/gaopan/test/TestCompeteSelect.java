package org.gaopan.test;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 * @author gaopan
 *
 * 竞技场刷新可挑战者</br>
 * 1. 按照参数分成5个档位</br>
 * 2. 每个档位选4个候选者</br>
 * 3. 以上方法选出来的集合作为一个缓存</br>
 * 4. 在一定时间段内(或一定次数内)刷新，都是从该缓存中的5个档位中随出5个被挑战者</br>
 * 5. 刷新一定次数后，或者缓存时间到后，缓存失效，回到第1步
 */
public class TestCompeteSelect {
	/** 每个档位4个备选者 */
	private static final int COUNT_PER_LEVEL	= 4;
	/** 档位数 */
	private static final int LEVEL_NUM			= 5;
	
	/** 默认参数(测试参数) */
	private static final float[] TEST_ARGS = {0.55f, 0.61f, 0.72f, 0.88f, 0.95f};
	private static final int TEST_SELF_RANK = 400;
	
	private List<JTextField> tfArgs = new ArrayList<>();
	private JTextField tfSelfRank = new JTextField(String.valueOf(TEST_SELF_RANK));
	private List<JTextField> tfResults = new ArrayList<>();
	
	private List<Float> args = new ArrayList<>();
	private int selfRank;
	
	private Random random = new Random();
	
	public TestCompeteSelect() {
		JFrame frame = new JFrame("TestCompeteSelect");
		frame.setSize(400, 300);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		JPanel argsPanel = new JPanel(new GridLayout(0, 2));
		argsPanel.setBorder(new TitledBorder("参数设置"));
		for(int i = 0; i < LEVEL_NUM; ++i) {
			argsPanel.add(new JLabel("参数" + (i + 1)));
			JTextField tfArg = new JTextField(String.valueOf(TEST_ARGS[i]));
			argsPanel.add(tfArg);
			tfArgs.add(tfArg);
		}
		argsPanel.add(new JLabel("自己排名"));
		argsPanel.add(tfSelfRank);
		JButton jbRefresh = new JButton("刷新");
		argsPanel.add(jbRefresh);
		frame.add(BorderLayout.NORTH, argsPanel);
		
		JPanel resultPanel = new JPanel(new FlowLayout());
		for(int i = 0; i < LEVEL_NUM; ++i) {
			JTextField tfResult = new JTextField(5);
			resultPanel.add(tfResult);
			tfResults.add(tfResult);
		}
		frame.add(BorderLayout.SOUTH, resultPanel);
		
		jbRefresh.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				getArgs();
				refresh();
			}
		});
	}
	
	/**
	 * 均匀划分(人数只满足最低要求时，均匀划分，每个档位4个人)
	 * @param rangeTop 起始名次
	 * @param selfRank
	 * @return
	 */
	private List<List<Integer>> splitAverage(int rangeTop, int selfRank) {
		List<List<Integer>> results = new ArrayList<>();
		for(int level = 0; level < LEVEL_NUM; ++level) {
			List<Integer> list = new ArrayList<>();
			for(int i = 0; i < COUNT_PER_LEVEL; ++i) {
				// 排除自己
				if(rangeTop == selfRank) {
					rangeTop++;
				}
				
				list.add(rangeTop++);
			}
			results.add(list);
		}
		
		return results;
	}
	
	/**
	 * 从0-range中随机挑num个不同的数
	 * @param range
	 * @param num
	 * @return
	 */
	private List<Integer> randomSelect(int range, int num) {
		List<Integer> results = new ArrayList<>();
		// 下限大于等于上限，返回空
		if(range <= 0) {
			return results;
		}
		
		// 数量不足，全部返回
		if(range <= num) {
			for(int i = 0; i < range; ++i) {
				results.add(i);
			}
			return results;
		}
		
		for(int i = 0; i < num; ++i) {
			int rnd = random.nextInt(range);
			while(results.contains(rnd)) {
				rnd++;
				if(rnd >= range) {
					rnd -= range;
				}
			}
			results.add(rnd);
		}
		
		return results;
	}
	
	/**
	 * 通过参数计算
	 * @param rangeTop
	 * @return
	 */
	private List<List<Integer>> splitByArgs(int rangeTop) {
		List<List<Integer>> results = new ArrayList<>();
		for(int level = 0; level < LEVEL_NUM; ++level) {
			List<Integer> list = new ArrayList<>();
			int rangeBotton;
			if(level != LEVEL_NUM - 1) {
				rangeBotton = (int)(selfRank * args.get(level + 1));
			} else {
				rangeBotton = selfRank;
			}
			
			List<Integer> tmp = randomSelect(rangeBotton - rangeTop, COUNT_PER_LEVEL);
			// 为空，至少选择一个(保证每个档位至少有一个被选者)
			if(tmp.isEmpty()) {
				list.add(rangeTop++);
			} else {
				for(int n : tmp) {
					list.add(n + rangeTop);
				}
			}
			
			results.add(list);
			rangeTop = Math.max(rangeTop, rangeBotton);
		}
		
		return results;
	}
	
	/**
	 * 获取输入参数
	 */
	private void getArgs() {
		try {
			for(int i = 0; i < LEVEL_NUM; ++i) {
				args.add(Float.parseFloat(tfArgs.get(i).getText()));
			}
			selfRank = Integer.parseInt(tfSelfRank.getText());
			
			if(args.get(0) >= args.get(1) || args.get(1) >= args.get(2) || args.get(2) >= args.get(3) || args.get(3) >= args.get(4) || selfRank <= 0) {
				JOptionPane.showMessageDialog(null, "参数非法", "哇哦！", JOptionPane.ERROR_MESSAGE);
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "参数非法", "哇哦！", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void refresh() {
		try {
			List<List<Integer>> results;
			// 排名在前21名时，返回排除自己后前20名的玩家
			if(selfRank <= COUNT_PER_LEVEL * LEVEL_NUM + 1) {
				results = splitAverage(1, selfRank);
			} else {
				// 上限(不包含)
				int rangeBotton = selfRank;
				// 下限(包含)
				int rangeTop = (int)(selfRank * args.get(0));
				int rangeTopLeast = rangeBotton - COUNT_PER_LEVEL * LEVEL_NUM;
				
				// 通过参数算出来的数量不足20个，取20个，做均匀分布
				if(rangeTop >= rangeTopLeast) {
					results = splitAverage(rangeTopLeast, selfRank);
				// 否则，按照参数计算
				} else {
					results = splitByArgs(rangeTop);
				}
			}
	
			for(int i = 0; i < LEVEL_NUM; ++i) {
				JTextField tf = tfResults.get(i);
				List<Integer> list = results.get(i);
				tf.setText(String.valueOf(list.get(random.nextInt(list.size()))));
			}
		} catch (Exception e) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			JOptionPane.showMessageDialog(null, stringWriter.getBuffer().toString(), "哇哦！", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				new TestCompeteSelect();
			}
		});
	}

}
