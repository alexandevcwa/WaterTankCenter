export interface WaterTank {
  id: number;
  minimumVolume: number;
  maximumVolume: number;
  currentVolume: number;
  currentPercentage: number;
  radio: number;
  height: number;
  diameter: number;
  filling: boolean;
}
