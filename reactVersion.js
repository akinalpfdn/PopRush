import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Play, RotateCcw, Trophy, ZoomIn, ZoomOut, Settings, Circle, Square, Hexagon, Triangle, Heart } from 'lucide-react';

// --- Constants ---
const GAME_DURATION = 60; // seconds
// Grid layout sum: 5+6+7+8+7+6+5 = 44
const TOTAL_BUBBLES = 44;

// 5-6-7-8-7-6-5 Layout mapping
let count = 0;
const Row1 = Array.from({ length: 5 }, () => count++);
const Row2 = Array.from({ length: 6 }, () => count++);
const Row3 = Array.from({ length: 7 }, () => count++);
const Row4 = Array.from({ length: 8 }, () => count++);
const Row5 = Array.from({ length: 7 }, () => count++);
const Row6 = Array.from({ length: 6 }, () => count++);
const Row7 = Array.from({ length: 5 }, () => count++);

const ROW_CONFIG = [Row1, Row2, Row3, Row4, Row5, Row6, Row7];

// Pastel Color Palette for the bubbles
const PASTEL_COLORS = [
  'bg-rose-300',   // Red-ish
  'bg-sky-300',    // Blue-ish
  'bg-emerald-300',// Green-ish
  'bg-amber-200',  // Yellow-ish
  'bg-violet-300'  // Purple-ish
];

const PASTEL_COLORS_PRESSED = [
  'bg-rose-400',
  'bg-sky-400',
  'bg-emerald-400',
  'bg-amber-300',
  'bg-violet-400'
];

// --- Sub-components ---

/**
 * Individual Bubble Component
 * Handles multiple shapes via CSS border-radius or clip-path.
 */
const Bubble = ({ id, isActive, isPressed, onPress, shape }) => {
  // Deterministic color based on ID to create a fixed pattern
  const colorIndex = id % PASTEL_COLORS.length;
  const baseColor = PASTEL_COLORS[colorIndex];
  const pressedColor = PASTEL_COLORS_PRESSED[colorIndex];

  // Helper for inner content (Glow/Light)
  const InnerContent = () => (
    <>
      <div 
        className={`
          absolute inset-0 bg-black mix-blend-multiply transition-opacity duration-150
          ${isActive && !isPressed ? 'opacity-20' : 'opacity-0'}
        `}
      />
      <div 
        className={`
          w-full h-full transition-all duration-200 absolute inset-0
          flex items-center justify-center
          ${isActive && !isPressed ? 'opacity-100 scale-100' : 'opacity-0 scale-90'}
        `}
      >
        <div className="w-4 h-4 sm:w-5 sm:h-5 bg-stone-800 rounded-full shadow-[0_0_12px_3px_rgba(0,0,0,0.5)] animate-pulse" />
      </div>
    </>
  );

  // --- Render based on Shape ---

  // 1. Standard CSS Shapes (Circle, Square) - support box-shadow best
  if (shape === 'circle' || shape === 'square') {
    return (
      <button
        onMouseDown={() => onPress(id)}
        onTouchStart={(e) => { e.preventDefault(); onPress(id); }}
        className={`
          relative w-10 h-10 sm:w-12 sm:h-12 transition-all duration-150 ease-out
          flex items-center justify-center overflow-hidden
          ${shape === 'circle' ? 'rounded-full' : 'rounded-xl'}
          ${isPressed 
            ? `${pressedColor} shadow-inner scale-95 translate-y-1`
            : `${baseColor} shadow-[0_4px_6px_-1px_rgba(0,0,0,0.1),0_2px_4px_-1px_rgba(0,0,0,0.06)] hover:-translate-y-0.5 active:scale-95 active:translate-y-0.5`
          }
        `}
        aria-label={`Bubble ${id}`}
      >
        <InnerContent />
      </button>
    );
  }

  // 2. Complex Shapes (Hexagon, Triangle, Heart) - use Clip-Path + Filter Drop-Shadow
  // Note: Clip-path removes standard box-shadow, so we use drop-shadow filter on parent or alternative styling.
  
  let clipPathValue = '';
  if (shape === 'hexagon') clipPathValue = 'polygon(50% 0, 100% 25%, 100% 75%, 50% 100%, 0 75%, 0 25%)';
  if (shape === 'triangle') clipPathValue = 'polygon(50% 0%, 0% 100%, 100% 100%)';
  if (shape === 'heart') clipPathValue = 'path("M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z")';

  // For complex shapes, we wrap the clipped element to apply a drop-shadow filter separately if needed, 
  // or simply rely on color change for pressed state since inset shadows are hard with clip-path.
  
  // To keep the layout consistent with the grid, we use a wrapper button for events
  return (
    <button
      onMouseDown={() => onPress(id)}
      onTouchStart={(e) => { e.preventDefault(); onPress(id); }}
      className="relative w-10 h-10 sm:w-12 sm:h-12 flex items-center justify-center outline-none"
      style={{ filter: isPressed ? 'none' : 'drop-shadow(0px 4px 2px rgba(0,0,0,0.1))' }}
      aria-label={`Bubble ${id}`}
    >
      <div 
        className={`
          w-full h-full transition-all duration-150 ease-out flex items-center justify-center
          ${isPressed ? `${pressedColor} scale-95 translate-y-1 brightness-90` : `${baseColor} hover:-translate-y-0.5 active:translate-y-0.5`}
        `}
        style={{ 
          clipPath: shape === 'heart' // Heart needs specific viewbox mapping or simpler polygon. Using a simpler Heart Polygon for robustness or SVG viewBox wrapper
           ? 'polygon(50% 15%, 61% 10%, 75% 10%, 90% 20%, 95% 35%, 90% 55%, 50% 90%, 10% 55%, 5% 35%, 10% 20%, 25% 10%, 39% 10%)' // Rough heart polygon
           : clipPathValue 
        }}
      >
        <InnerContent />
      </div>
    </button>
  );
};

// --- Main Application ---

export default function App() {
  // Game State
  const [isPlaying, setIsPlaying] = useState(false);
  const [gameOver, setGameOver] = useState(false);
  const [score, setScore] = useState(0);
  const [highScore, setHighScore] = useState(0);
  const [timeLeft, setTimeLeft] = useState(GAME_DURATION);
  const [scale, setScale] = useState(1);
  
  // Settings State
  const [shape, setShape] = useState('circle'); // circle, square, hexagon, triangle, heart
  const [showSettings, setShowSettings] = useState(false);

  // Logic State
  const [activeBubbles, setActiveBubbles] = useState([]);
  const [pressedBubbles, setPressedBubbles] = useState([]);
  
  const timerRef = useRef(null);

  // Initialize Game
  const startGame = () => {
    setIsPlaying(true);
    setGameOver(false);
    setScore(0);
    setTimeLeft(GAME_DURATION);
    setPressedBubbles([]);
    generateLevel(0);
    setShowSettings(false); // Close menu on start
    
    if (timerRef.current) clearInterval(timerRef.current);
    timerRef.current = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          endGame();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const endGame = () => {
    clearInterval(timerRef.current);
    setIsPlaying(false);
    setGameOver(true);
  };

  useEffect(() => {
    if (gameOver) {
      if (score > highScore) {
        setHighScore(score);
        localStorage.setItem('fastPushHighScore', score.toString());
      }
    }
  }, [gameOver, score, highScore]);

  useEffect(() => {
    const saved = localStorage.getItem('fastPushHighScore');
    if (saved) setHighScore(parseInt(saved));
  }, []);

  const generateLevel = useCallback(() => {
    const numToLight = Math.floor(Math.random() * 4) + 4;
    const newActive = [];
    while (newActive.length < numToLight) {
      const r = Math.floor(Math.random() * TOTAL_BUBBLES);
      if (!newActive.includes(r)) newActive.push(r);
    }
    setActiveBubbles(newActive);
    setPressedBubbles([]);
  }, []);

  const handleBubblePress = (id) => {
    if (!isPlaying) return;
    if (activeBubbles.includes(id) && !pressedBubbles.includes(id)) {
      const newPressed = [...pressedBubbles, id];
      setPressedBubbles(newPressed);
      const remainingActive = activeBubbles.filter(b => !newPressed.includes(b));
      if (remainingActive.length === 0) {
        setTimeout(() => {
            setScore(s => s + 1);
            generateLevel();
        }, 100);
      }
    }
  };

  // Zoom handlers
  const zoomIn = () => setScale(s => Math.min(s + 0.1, 1.5));
  const zoomOut = () => setScale(s => Math.max(s - 0.1, 0.5));

  // Settings handlers
  const toggleSettings = () => setShowSettings(!showSettings);
  const selectShape = (s) => {
    setShape(s);
    setShowSettings(false);
  };

  return (
    <div className="min-h-screen bg-stone-50 flex items-center justify-center p-4 font-sans select-none touch-manipulation relative overflow-hidden">
      
      {/* Scale Wrapper */}
      <div style={{ transform: `scale(${scale})`, transition: 'transform 0.2s ease-out' }}>
        <div className="w-full max-w-lg bg-white rounded-[3rem] p-6 shadow-2xl relative overflow-hidden flex flex-col items-center">
          
          {/* Header */}
          <div className="w-full flex justify-between items-center mb-8 px-4">
            <div className="flex flex-col items-start">
              <span className="text-[0.7rem] uppercase font-bold text-stone-400 tracking-widest">Score</span>
              <span className="text-3xl font-black text-stone-700">{score}</span>
            </div>
            <div className="flex flex-col items-center">
               <div className="w-16 h-16 rounded-full bg-stone-100 flex items-center justify-center shadow-inner">
                   <span className={`text-2xl font-bold ${timeLeft < 10 ? 'text-rose-400 animate-pulse' : 'text-stone-600'}`}>
                     {timeLeft}
                   </span>
               </div>
            </div>
            <div className="flex flex-col items-end">
              <span className="text-[0.7rem] uppercase font-bold text-stone-400 tracking-widest">Best</span>
              <span className="text-xl font-black text-amber-400">{highScore}</span>
            </div>
          </div>

          {/* Game Grid */}
          <div className="flex flex-col items-center gap-3 mb-6">
            {ROW_CONFIG.map((row, rowIndex) => (
              <div key={rowIndex} className="flex gap-3">
                {row.map(id => (
                  <Bubble 
                    key={id} 
                    id={id} 
                    shape={shape}
                    isActive={activeBubbles.includes(id)}
                    isPressed={pressedBubbles.includes(id)}
                    onPress={handleBubblePress}
                  />
                ))}
              </div>
            ))}
          </div>
            
          {/* Menu Overlay */}
          {!isPlaying && !gameOver && (
            <div className="absolute inset-0 bg-white/60 backdrop-blur-sm z-10 flex flex-col items-center justify-center rounded-[3rem]">
              <h1 className="text-4xl font-black text-stone-700 mb-2 tracking-tighter">POP IT</h1>
              <p className="text-stone-400 mb-8 font-bold tracking-widest text-xs uppercase">Speed Challenge</p>
              <button 
                onClick={startGame}
                className="flex items-center gap-3 bg-stone-800 hover:bg-black text-white px-10 py-4 rounded-full font-bold shadow-lg transition-transform active:scale-95"
              >
                <Play size={24} fill="currentColor" /> PLAY
              </button>
            </div>
          )}

          {/* Game Over Overlay */}
          {gameOver && (
            <div className="absolute inset-0 bg-stone-900/90 backdrop-blur-md z-10 flex flex-col items-center justify-center rounded-[3rem] text-white animate-in fade-in duration-300">
              <div className="w-20 h-20 bg-yellow-400 rounded-full flex items-center justify-center mb-4 shadow-[0_0_30px_rgba(250,204,21,0.5)]">
                 <Trophy size={40} className="text-white" />
              </div>
              <h2 className="text-3xl font-bold mb-1">Time's Up!</h2>
              <div className="text-6xl font-black text-transparent bg-clip-text bg-gradient-to-tr from-rose-300 to-amber-300 mb-8">
                  {score}
              </div>
              <button 
                onClick={startGame}
                className="flex items-center gap-2 bg-white text-stone-900 px-8 py-3 rounded-full font-bold shadow-xl hover:bg-gray-100 transition-transform active:scale-95"
              >
                <RotateCcw size={20} /> TRY AGAIN
              </button>
            </div>
          )}
          
        </div>
      </div>
      
      {/* Bottom Controls (Settings & Zoom) */}
      <div className="fixed bottom-4 right-4 flex gap-2 z-50 items-end">
        
        {/* Settings Dropdown */}
        <div className="relative">
            {showSettings && (
                <div className="absolute bottom-full right-0 mb-2 bg-white rounded-2xl shadow-xl p-2 flex flex-col gap-1 min-w-[140px] animate-in slide-in-from-bottom-2">
                    <div className="text-xs font-bold text-stone-400 px-3 py-2 uppercase tracking-wider">Shape</div>
                    <button onClick={() => selectShape('circle')} className={`flex items-center gap-2 p-2 rounded-xl text-sm font-bold ${shape === 'circle' ? 'bg-stone-100 text-stone-800' : 'text-stone-500 hover:bg-stone-50'}`}>
                        <Circle size={16} /> Circle
                    </button>
                    <button onClick={() => selectShape('square')} className={`flex items-center gap-2 p-2 rounded-xl text-sm font-bold ${shape === 'square' ? 'bg-stone-100 text-stone-800' : 'text-stone-500 hover:bg-stone-50'}`}>
                        <Square size={16} /> Square
                    </button>
                    <button onClick={() => selectShape('hexagon')} className={`flex items-center gap-2 p-2 rounded-xl text-sm font-bold ${shape === 'hexagon' ? 'bg-stone-100 text-stone-800' : 'text-stone-500 hover:bg-stone-50'}`}>
                        <Hexagon size={16} /> Hexagon
                    </button>
                     <button onClick={() => selectShape('triangle')} className={`flex items-center gap-2 p-2 rounded-xl text-sm font-bold ${shape === 'triangle' ? 'bg-stone-100 text-stone-800' : 'text-stone-500 hover:bg-stone-50'}`}>
                        <Triangle size={16} /> Triangle
                    </button>
                    <button onClick={() => selectShape('heart')} className={`flex items-center gap-2 p-2 rounded-xl text-sm font-bold ${shape === 'heart' ? 'bg-stone-100 text-stone-800' : 'text-stone-500 hover:bg-stone-50'}`}>
                        <Heart size={16} /> Heart
                    </button>
                </div>
            )}
            <button 
              onClick={toggleSettings}
              className={`bg-white/80 p-3 rounded-full shadow-lg hover:bg-white active:scale-95 transition-all ${showSettings ? 'text-stone-900 bg-white ring-2 ring-stone-200' : 'text-stone-600'}`}
              aria-label="Settings"
            >
              <Settings size={24} />
            </button>
        </div>

        <button 
          onClick={zoomOut}
          className="bg-white/80 p-3 rounded-full shadow-lg text-stone-600 hover:bg-white active:scale-95 transition-all"
          aria-label="Zoom Out"
        >
          <ZoomOut size={24} />
        </button>
        <button 
          onClick={zoomIn}
          className="bg-white/80 p-3 rounded-full shadow-lg text-stone-600 hover:bg-white active:scale-95 transition-all"
          aria-label="Zoom In"
        >
          <ZoomIn size={24} />
        </button>
      </div>

      <div className="fixed bottom-4 left-1/2 -translate-x-1/2 text-stone-400 text-xs font-semibold tracking-wider opacity-50 whitespace-nowrap pointer-events-none">
        MADE BY MOVI
      </div>
    </div>
  );
}