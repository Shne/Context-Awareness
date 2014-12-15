package jhk.context_awareness;

import java.util.ArrayList;

/**
 * Created by Roland on 15-12-2014.
 */
public class ClassifierSequence {

    private ArrayList<MovementType> sequence;
    int sequenceLength;

    public ClassifierSequence(int length) {
        sequenceLength = length;
        sequence = new ArrayList<MovementType>(sequenceLength);
    }

    public void add(MovementType mt){
        if(sequence.size() < sequenceLength){
            sequence.add(mt);
        }else{
            sequence.remove(sequence.get(0));
            sequence.add(mt);
        }
    }

    public MovementType getLatestClassifiedMovement(){
        if(sequence.size() > 0){
            return sequence.get(sequence.size()-1);
        }else{
            return null;
        }
    }

    public int numberOfElements(){
        return sequence.size();
    }

    public ArrayList<MovementType> getClassifierSequence(){
        return sequence;
    }




}
