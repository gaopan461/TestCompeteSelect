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
 * ������ˢ�¿���ս��</br>
 * 1. ���ղ����ֳ�5����λ</br>
 * 2. ÿ����λѡ4����ѡ��</br>
 * 3. ���Ϸ���ѡ�����ļ�����Ϊһ������</br>
 * 4. ��һ��ʱ�����(��һ��������)ˢ�£����ǴӸû����е�5����λ�����5������ս��</br>
 * 5. ˢ��һ�������󣬻��߻���ʱ�䵽�󣬻���ʧЧ���ص���1��
 */
public class TestCompeteSelect {
	/** ÿ����λ4����ѡ�� */
	private static final int COUNT_PER_LEVEL	= 4;
	/** ��λ�� */
	private static final int LEVEL_NUM			= 5;
	
	/** Ĭ�ϲ���(���Բ���) */
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
		argsPanel.setBorder(new TitledBorder("��������"));
		for(int i = 0; i < LEVEL_NUM; ++i) {
			argsPanel.add(new JLabel("����" + (i + 1)));
			JTextField tfArg = new JTextField(String.valueOf(TEST_ARGS[i]));
			argsPanel.add(tfArg);
			tfArgs.add(tfArg);
		}
		argsPanel.add(new JLabel("�Լ�����"));
		argsPanel.add(tfSelfRank);
		JButton jbRefresh = new JButton("ˢ��");
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
	 * ���Ȼ���(����ֻ�������Ҫ��ʱ�����Ȼ��֣�ÿ����λ4����)
	 * @param rangeTop ��ʼ����
	 * @param selfRank
	 * @return
	 */
	private List<List<Integer>> splitAverage(int rangeTop, int selfRank) {
		List<List<Integer>> results = new ArrayList<>();
		for(int level = 0; level < LEVEL_NUM; ++level) {
			List<Integer> list = new ArrayList<>();
			for(int i = 0; i < COUNT_PER_LEVEL; ++i) {
				// �ų��Լ�
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
	 * ��0-range�������num����ͬ����
	 * @param range
	 * @param num
	 * @return
	 */
	private List<Integer> randomSelect(int range, int num) {
		List<Integer> results = new ArrayList<>();
		// ���޴��ڵ������ޣ����ؿ�
		if(range <= 0) {
			return results;
		}
		
		// �������㣬ȫ������
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
	 * ͨ����������
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
			// Ϊ�գ�����ѡ��һ��(��֤ÿ����λ������һ����ѡ��)
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
	 * ��ȡ�������
	 */
	private void getArgs() {
		try {
			for(int i = 0; i < LEVEL_NUM; ++i) {
				args.add(Float.parseFloat(tfArgs.get(i).getText()));
			}
			selfRank = Integer.parseInt(tfSelfRank.getText());
			
			if(args.get(0) >= args.get(1) || args.get(1) >= args.get(2) || args.get(2) >= args.get(3) || args.get(3) >= args.get(4) || selfRank <= 0) {
				JOptionPane.showMessageDialog(null, "�����Ƿ�", "��Ŷ��", JOptionPane.ERROR_MESSAGE);
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "�����Ƿ�", "��Ŷ��", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void refresh() {
		try {
			List<List<Integer>> results;
			// ������ǰ21��ʱ�������ų��Լ���ǰ20�������
			if(selfRank <= COUNT_PER_LEVEL * LEVEL_NUM + 1) {
				results = splitAverage(1, selfRank);
			} else {
				// ����(������)
				int rangeBotton = selfRank;
				// ����(����)
				int rangeTop = (int)(selfRank * args.get(0));
				int rangeTopLeast = rangeBotton - COUNT_PER_LEVEL * LEVEL_NUM;
				
				// ͨ���������������������20����ȡ20���������ȷֲ�
				if(rangeTop >= rangeTopLeast) {
					results = splitAverage(rangeTopLeast, selfRank);
				// ���򣬰��ղ�������
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
			JOptionPane.showMessageDialog(null, stringWriter.getBuffer().toString(), "��Ŷ��", JOptionPane.ERROR_MESSAGE);
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
